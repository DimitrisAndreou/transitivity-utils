package edu.bath.transitivityutils;

import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Efficient implementation of {@link OrderList}.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 * @see <a href="http://portal.acm.org/citation.cfm?id=740822">Two Simplified Algorithms for Maintaining Order in a List (Bender et al., 2002)</a>
 */
public class BenderList<E> implements OrderList<E>, Iterable<E>, Serializable {
    private transient /*final*/ Node<E> base;
    private transient int size = 0;
    
    private static final long serialVersionUID = -6060298699521132512L;

    private BenderList() {
        init();
    }

    private void init() {
        base = new Node<E>(null, Long.MIN_VALUE);
    }

    public static <E> BenderList<E> create() {
        return new BenderList<E>();
    }

    public OrderList.Node<E> base() {
        return base;
    }

    public boolean delete(OrderList.Node<E> node) {
        Node<E> n = (Node<E>)node;
        if (!n.isValid()) return false;
        if (node == base) return false;
        n.prev.next = n.next;
        n.next.prev = n.prev;
        n.prev = n.next = null;

        size--;
        return true;
    }

    public int size() {
        return size;
    }

    public OrderList.Node<E> addAfter(OrderList.Node<E> node, E value) {
        Node<E> n = ((Node<E>)node);
        Preconditions.checkState(n.isValid(), "Node has been deleted");
        Preconditions.checkState(size != Integer.MAX_VALUE, "Too many elements"); //just for good conscience; never going to happen

        final long newTag;
        if (n.next == n) { //then this node is the base (with tag of Long.MIN_VALUE) and we insert the first real node
            newTag = 0L;
        } else {
            if (n.tag + 1 == n.next.tag) {
                relabelMinimumSparseEnclosingRange(n);
            }
            if (n.next == base) {
                if (n.tag != Long.MAX_VALUE - 1) {
                    newTag = average(n.tag, Long.MAX_VALUE);
                } else {
                    newTag = Long.MAX_VALUE; //caution: in this case average(n.tag, Long.MAX_VALUE) here would just return n.tag again!
                }
            } else {
                newTag = average(n.tag, n.next.tag);
            }
        }
        Node<E> newNode = new Node<E>(value, newTag);
        newNode.prev = n;
        newNode.next = n.next;

        n.next = newNode;
        newNode.next.prev = newNode;
        size++;
        return newNode;
    }

    private static long average(long x, long y) {
        return (x & y) + (x ^ y) / 2;
    }

    private double computeOptimalT() {
        //note that division by zero is impossible, since with size == 1, no relabeling
        return Math.pow(Math.pow(2, 62) / size, 1.0 / 62);
    }

    //seek an enclosing range with the appropriate density, and relabel it
    private void relabelMinimumSparseEnclosingRange(Node<E> n) {
        final double T = computeOptimalT();

        double elementCount = 1.0;

        Node<E> left = n;
        Node<E> right = n;
        long low = n.tag;
        long high = n.tag;

        int level = 0;
        double overflowThreshold = 1.0;
        long range = 1;
        do {
            long toggleBit = 1L << level++;
            overflowThreshold /= T;
            range <<= 1;

            boolean expandToLeft = (n.tag & toggleBit) != 0L;
            if (expandToLeft) {
                low ^= toggleBit;
                while (left.tag > low) {
                    left = left.prev;
                    elementCount++;
                }
            } else {
                high ^= toggleBit;
                while (right.tag < high && right.next.tag > right.tag) {
                    right = right.next;
                    elementCount++;
                }
            }
        } while (elementCount >= (range * overflowThreshold) && level < 62);
        int count = (int)elementCount; //elementCount always fits into an int, size() is an int too

        //note that the base itself can be relabeled, but always gets the same label! (Long.MIN_VALUE)
        long pos = low;
        long step = range / count;
        Node<E> cursor = left;
        if (step > 1) {
            for (int i = 0; i < count; i++) {
                cursor.tag = pos;
                pos += step;
                cursor = cursor.next;
            }
        } else { //handle degenerate case here (step == 1)
            //make sure that this and next are separated by distance of at least 2
            long slack = range - count;
            for (int i = 0; i < elementCount; i++) {
                cursor.tag = pos;
                pos++;
                if (n == cursor) {
                    pos += slack;
                }
                cursor = cursor.next;
            }
        }
        assert n.tag + 1 != n.next.tag;
    }

    public Iterator<E> iterator() {
        return new IteratorImpl(base);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Node<E> n = base.next;
        sb.append("[");
        while (n != base) {
            sb.append(n.value).append("(").append(n.tag).append(")");
            if (n.next != base) {
                sb.append(", ");
            }
            n = n.next;
        }
        sb.append("]");
        return sb.toString();
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

    static class Node<E> implements OrderList.Node<E> {
        long tag;
        private Node<E> prev;
        private Node<E> next;
        private E value;
        
        protected Node(E value, long tag) {
            this.value = value;
            this.tag = tag;
            this.prev = this;
            this.next = this;
        }

        public final boolean precedes(OrderList.Node<?> n) {
            return tag < ((Node<?>)n).tag;
        }
        
        public final boolean isValid() {
            return prev != null;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public final OrderList.Node<E> next() {
            return next;
        }

        public final OrderList.Node<E> previous() {
            return prev;
        }

        public final E get() {
            return value;
        }

        public final E set(E newValue) {
            E old = value;
            value = newValue;
            return old;
        }
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.writeInt(size);
        int total = 0;
        for (E element : this) {
            total++;
            s.writeObject(element);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        init();
        int count = s.readInt();
        s.defaultReadObject();
        for (int i = 0; i < count; i++) {
            addAfter(base.previous(), (E)s.readObject());
        }
    }
}
