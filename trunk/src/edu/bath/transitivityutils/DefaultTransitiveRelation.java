package edu.bath.transitivityutils;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A (transitive, reflexive) binary relation.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
class DefaultTransitiveRelation<E> implements TransitiveRelation<E>, Serializable {
    private final OrderList<E> magicList = OrderList.create();
    private final Map<E, Node<E>> nodeMap = Maps.newHashMap();
    private final SetMultimap<Node<E>, Node<E>> directRelationships = HashMultimap.create();
    private final Navigator<E> navigator = new DirectNavigator();

    private static final long serialVersionUID = -4031451040065579682L;

    DefaultTransitiveRelation() { }

    public void relate(E subjectValue, E objectValue) {
        if (Objects.equal(subjectValue, objectValue)) {
            return;
        }
        Node<E> subject = nodeMap.get(subjectValue);
        Node<E> object = createObjectNode(objectValue, subject);

        if (subject != null) {
            propagate(subject, object);
        } else {
            OrderList.Node<E> anchor = object.post.previous();
            subject = new Node<E>(
                    anchor = magicList.addAfter(anchor, subjectValue),
                    magicList.addAfter(anchor, null)); //we don't need the value in post nodes, we get it from pre nodes
            nodeMap.put(subjectValue, subject);
        }
        directRelationships.put(subject, object);
    }

    private Node<E> createObjectNode(E value, Node<E> subject) {
        Node<E> node = nodeMap.get(value);
        if (node == null) {
            if (subject != null && !directRelationships.containsKey(subject)) {
                //subject.pre, post is not embedded in another node, so it is possible
                //to surround it by the new node
                node = new Node<E>(
                        magicList.addAfter(subject.pre.previous(), value),
                        magicList.addAfter(subject.post, null)); //we don't need the value in post nodes, we get it from pre nodes
            } else {
                node = new Node<E>(
                    magicList.addAfter(magicList.base().previous(), value),
                    magicList.addAfter(magicList.base().previous(), null)); //we don't need the value in post nodes, we get it from pre nodes
            }
            nodeMap.put(value, node);
        }
        return node;
    }

    private void propagate(Node<E> subject, Node<E> object) {
        Iterator<Node<E>> toVisit = Iterators.singletonIterator(object);
        while (toVisit.hasNext()) {
            Node<E> next = toVisit.next();
            if (next.intervalSet.contains(subject.intervalSet)) {
                continue; //this gracefully handles cycles
            }
            next.intervalSet.addIntervals(subject.intervalSet);
            toVisit = Iterators.concat(directRelationships.get(next).iterator(), toVisit);
        }
    }

    public boolean areRelated(E subjectValue, E objectValue) {
        if (Objects.equal(subjectValue, objectValue)) return true;

        Node<E> subject = nodeMap.get(subjectValue);
        if (subject == null) return false;

        Node<E> object = nodeMap.get(objectValue);
        if (object == null) return false;
        
        return areNodesRelated(subject, object);
    }

    private boolean areNodesRelated(Node<E> subject, Node<E> object) {
        return object.intervalSet.contains(subject.pre);
    }

    public Navigator<E> direct() {
        return navigator;
    }

    @Override
    public String toString() {
        return nodeMap.toString();
    }

    private static class Node<E> {
        final OrderList.Node<E> pre;
        final OrderList.Node<E> post;

        final MergingIntervalSet intervalSet = new MergingIntervalSet();

        Node(OrderList.Node<E> pre, OrderList.Node<E> post) {
            this.pre = pre;
            this.post = post;
            intervalSet.addInterval(pre, post);
        }

        @Override
        public String toString() {
            return intervalSet.toString();
        }
    }

    private class DirectNavigator implements Navigator<E> {
        private final Function<Node<E>, E> nodeToValue = new Function<Node<E>, E>() {
            public E apply(Node<E> node) {
                return magicList.get(node.pre);
            }
        };
        
        public Set<E> related(E subjectValue) {
            Node<E> subject = nodeMap.get(subjectValue);
            if (subject == null) return Collections.emptySet();

            final Set<Node<E>> set = directRelationships.get(subject);
            return transformSet(set, nodeToValue);
        }

        public Set<E> domain() {
            return transformSet(directRelationships.keySet(), nodeToValue);
        }
    }
    
    private Object writeReplace() {
        return new SerializationProxy<E>(navigator);
    }

    private static class SerializationProxy<E> implements Serializable {
        transient Navigator<E> navigator;
        
        private static final long serialVersionUID = 711361401943593391L;

        SerializationProxy() { }
        SerializationProxy(Navigator<E> navigator) {
            this.navigator = navigator;
        }

        //Writing the number of domain elements, then iterate over the domain and write:
        // - the domain element
        // - the number of related (to that) elements
        // - the related elements themselves
        private void writeObject(ObjectOutputStream s) throws IOException {
            Set<E> domain = navigator.domain();
            s.writeInt(domain.size());
            for (E subject : domain) {
                s.writeObject(subject);
                Set<E> related = navigator.related(subject);
                s.writeInt(related.size());
                for (E object : related) {
                    s.writeObject(object);
                }
            }
        }

        @SuppressWarnings("unchecked")
        private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
            SetMultimap<Object, Object> mm = HashMultimap.create();
            int domainCount = s.readInt();
            for (int i = 0; i < domainCount; i++) {
                Object subject = s.readObject();
                Collection<Object> objects = mm.get(subject);
                int objectCount = s.readInt();
                for (int j = 0; j < objectCount; j++) {
                    Object object = s.readObject();
                    mm.put(object, object);
                    objects.add(object);
                }
            }
            navigator = (Navigator)Navigators.forMultimap(mm);
        }

        private Object readResolve() {
            DefaultTransitiveRelation<E> rel = new DefaultTransitiveRelation<E>();
            for (E subject : navigator.domain()) {
                for (E object : navigator.related(subject)) {
                    rel.relate(subject, object);
                }
            }
            return rel;
        }
    }
    
    static <A, B> Set<B> transformSet(final Set<A> set, final Function<? super A, ? extends B> transformer) {
        return new AbstractSet<B>() {
            @Override
            public Iterator<B> iterator() {
                return Iterators.transform(set.iterator(), transformer);
            }

            @Override
            public int size() {
                return set.size();
            }
        };
    }
}
