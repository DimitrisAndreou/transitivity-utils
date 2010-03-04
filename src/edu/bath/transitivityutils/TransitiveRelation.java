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
 * A transitive binary relation.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public final class TransitiveRelation<E> {
    private final BenderList<E> magicList = BenderList.create();
    private final Map<E, Node<E>> nodes = Maps.newHashMap();
    private final Multimap<Node<E>, Node<E>> edges = HashMultimap.create();

    private TransitiveRelation() { }

    public static <E> TransitiveRelation<E> create() {
        return new TransitiveRelation<E>();
    }

    public void relate(E subjectValue, E objectValue) {
        if (Objects.equal(subjectValue, objectValue)) {
            return;
        }
        Node<E> subject = nodes.get(subjectValue);
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
            nodes.put(subjectValue, subject);
        }
        if (subject != object) {
            edges.put(subject, object);
        }
    }

    private Node<E> createObjectNode(E value, Node<E> subject) {
        Node<E> node = nodes.get(value);
        if (node == null) {
            if (subject != null && !edges.containsKey(subject)) {
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
            nodes.put(value, node);
        }
        return node;
    }

    private void propagate(Node<E> subject, Node<E> object) {
        Iterator<Node<E>> toVisit = Iterators.singletonIterator(object);
        while (toVisit.hasNext()) {
            Node<E> next = toVisit.next();
            if (!next.contains(subject)) {
                next.intervalSet.addIntervals(subject.intervalSet);
                toVisit = Iterators.concat(edges.get(next).iterator(), toVisit);
            }
        }
    }

    public boolean areRelated(E subjectValue, E objectValue) {
        if (Objects.equal(subjectValue, objectValue)) return true;
        Node<E> subject = nodes.get(subjectValue);
        if (subject == null) return false;
        Node<E> object = nodes.get(objectValue);
        if (object == null) return false;
        return areNodesRelated(subject, object);
    }

    public boolean areDirectlyRelated(E subjectValue, E objectValue) {
        if (Objects.equal(subjectValue, objectValue)) return true;
        Node<E> subject = nodes.get(subjectValue);
        if (subject == null) return false;
        Node<E> object = nodes.get(objectValue);
        if (object == null) return false;
        return edges.containsEntry(subject, object);
    }

    private final Function<Node<E>, E> nodeToValue = new Function<Node<E>, E>() {
        public E apply(Node<E> node) {
            return node.pre.get();
        }
    };
    public Collection<E> directlyRelatedWith(E subjectValue) {
        Node<E> subject = nodes.get(subjectValue);
        if (subject == null) return Collections.emptySet();

        return Collections.unmodifiableCollection(Collections2.transform(edges.get(subject), nodeToValue));
    }

    private boolean areNodesRelated(Node<E> subject, Node<E> object) {
        return object.contains(subject);
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
}
