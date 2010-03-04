package edu.bath.transitivityutils;

import com.google.common.base.Preconditions;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementation of algorithm of Section 2 of
 * <a href="http://portal.acm.org/citation.cfm?id=28434">Two algorithms for maintaining order in a list (Dietz, Sleator, 1989)</a>
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
class DietzList<E> implements OrderList<E> {
    private final Node<E> base = new Node<E>();
    private int size = 1; //set to zero if possible
    long relabels = 0;

    public DietzList() { }

    private long labelOfSuccessor(Node r) {
        return (r.next == base) ? Long.MAX_VALUE : r.next.tag;
    }

    public OrderList.Node<E> addAfter(OrderList.Node<E> node, E value) {
        Node<E> n = (Node<E>)node;
        Preconditions.checkState(n.isValid(), "Node has been deleted");

        Node<E> y = new Node<E>(value, n, n.next);

        if (n.tag + 2 > labelOfSuccessor(n)) {
            long i = 1;
            long j = 2;
            while (n.w(j, size) <= 4 * n.w(i, size)) {
                i++;
                j = Math.min(2 * i, size);
            }

            long wj = n.w(j, size);
            Node curr = n.next;
            for (int k = 1; k <= j - 1; k++) {
                curr.tag = wj * k / j + n.tag;
                curr = curr.next;
                relabels++;
            }
        }
        size++;
        y.tag = n.tag / 2 + labelOfSuccessor(n) / 2;
        n.next.prev = y;
        n.next = y;
        return y;
    }

    public OrderList.Node<E> base() {
        return base;
    }

    public int size() {
        return size;
    }

    public boolean delete(OrderList.Node<E> node) {
        Node<E> n = (Node<E>)node;
        if (n.isValid()) return false;
        if (n == base) return false;
        n.prev = n.next = null;
        size--;
        return true;
    }

    public Iterator<E> iterator() {
        return new IteratorImpl(base);
    }

    private class IteratorImpl implements Iterator<E> {
        private Node<E> node;

        IteratorImpl(Node<E> node) {
            this.node = node;
        }

        public boolean hasNext() {
            return node.next != base;
        }

        public E next() {
            if (!hasNext()) throw new NoSuchElementException();
            try {
                return node.next.value;
            } finally {
                node = node.next;
            }
        }

        public void remove() {
            delete(node);
        }
    }

    private static class Node<E> implements OrderList.Node<E> {
        private Node<E> prev;
        private Node<E> next;
        private long tag;
        private E value;

        private Node() {
            this.next = this;
            this.prev = this;
        }

        private Node(E value, Node<E> prev, Node<E> next) {
            this.value = value;
            this.prev = prev;
            this.next = next;
        }

        Node<E> successor(long offset) {
            Node<E> curr = this;
            while (offset-- > 0) {
                curr = curr.next;
            }
            return curr;
        }

        long w(long offset, long size) {
            if (offset == size) return Long.MAX_VALUE;
            return successor(offset).tag - this.tag;
        }

        @Override
        public String toString() {return "" + tag;}

        public boolean precedes(OrderList.Node<?> n) {
            return tag < ((Node<?>)n).tag;
        }

        public boolean isValid() {
            return prev != null;
        }

        public OrderList.Node<E> next() {
            return next;
        }

        public OrderList.Node<E> previous() {
            return prev;
        }

        public E get() {
            return value;
        }

        public E set(E newValue) {
            E old = value;
            value = newValue;
            return old;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Node curr = base.next;
        sb.append("[");
        while (curr != base) {
            sb.append(curr.tag);
            if (curr.next != base) {
                sb.append(", ");
            }
            curr = curr.next;
        }
        sb.append("]");
        return sb.toString();
    }

    void validate() {
        Node curr = base.next;
        while (curr.next != base) {
            if (curr.tag >= curr.next.tag) {
                System.out.println(curr.tag + " >= " + curr.next.tag);
            }
            curr = curr.next;
        }
    }
}
