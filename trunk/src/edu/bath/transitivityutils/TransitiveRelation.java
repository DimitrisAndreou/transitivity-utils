package edu.bath.transitivityutils;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * A (transitive, reflexive) binary relation.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public class TransitiveRelation<E> implements Relation<E> {
    private final BenderList<E> magicList = BenderList.create();
    private final Map<E, Node<E>> nodeMap = Maps.newHashMap();
    private final Multimap<Node<E>, Node<E>> directRelationships = HashMultimap.create();
    private final Navigator<E> navigator = new DirectNavigator();

    TransitiveRelation() { }

    public static <E> TransitiveRelation<E> create() {
        return new TransitiveRelation<E>();
    }

    public void relate(E subjectValue, E objectValue) {
        if (Objects.equal(subjectValue, objectValue)) {
            return;
        }
        Node<E> subject = nodeMap.get(subjectValue);
        Node<E> object = createObjectNode(objectValue, subject);

        if (subject != null) {
            if (!areNodesRelated(subject, object)) {
                propagate(subject, object);
            }
        } else {
            OrderList.Node<E> anchor = object.post.previous();
            subject = new Node<E>(
                    anchor = magicList.addAfter(anchor, subjectValue),
                    magicList.addAfter(anchor, subjectValue));
            nodeMap.put(subjectValue, subject);
        }
        directRelationships.put(subject, object);
        recordRelationship(subjectValue, objectValue);
        return;
    }

    void recordRelationship(E subjectValue, E objectValue) { }

    private Node<E> createObjectNode(E value, Node<E> subject) {
        Node<E> node = nodeMap.get(value);
        if (node == null) {
            if (subject != null && !directRelationships.containsKey(subject)) {
                //subject.pre, post is embedded in another node, so it is possible
                //to surround it by the new node
                node = new Node<E>(
                        magicList.addAfter(subject.pre.previous(), value),
                        magicList.addAfter(subject.post, value));
            } else {
                node = new Node<E>(
                    magicList.addAfter(magicList.base().previous(), value),
                    magicList.addAfter(magicList.base().previous(), value));
            }
            nodeMap.put(value, node);
        }
        return node;
    }

    private void propagate(Node<E> subject, Node<E> object) {
        Iterator<Node<E>> toVisit = Iterators.singletonIterator(object);
        while (toVisit.hasNext()) {
            Node<E> next = toVisit.next();
            if (!next.contains(subject)) {
                next.intervalSet.addIntervals(subject.intervalSet);
                toVisit = Iterators.concat(directRelationships.get(next).iterator(), toVisit);
            }
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
        return object.contains(subject);
    }

    public Navigator<E> direct() {
        return navigator;
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

        boolean contains(Node<E> other) {
            return intervalSet.contains(other.pre);
        }

        @Override
        public String toString() {
            return "[" + pre + ", " + post + "]";
        }
    }

    private class DirectNavigator implements Navigator<E> {
        private final Function<Node<E>, E> nodeToValue = new Function<Node<E>, E>() {
            public E apply(Node<E> node) {
                return node.pre.get();
            }
        };
        
        public Collection<E> related(E subjectValue) {
            Node<E> subject = nodeMap.get(subjectValue);
            if (subject == null) return Collections.emptySet();

            return Collections.unmodifiableCollection(Collections2.transform(directRelationships.get(subject), nodeToValue));
        }

        public Collection<E> domain() {
            return Collections2.transform(directRelationships.keySet(), nodeToValue);
        }
    }
}
