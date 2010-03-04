package edu.bath.transitivityutils;

import edu.bath.transitivityutils.OrderList.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public class BenderListTest {
    private interface Chooser {
        <T> T choose(List<T> list);
    }

    private static final Chooser leftChooser = new Chooser() {
        public <T> T choose(List<T> list) {
            return list.get(0);
        }
    };

    private static final Chooser rightChooser = new Chooser() {
        public <T> T choose(List<T> list) {
            return list.get(list.size() - 1);
        }
    };

    private static final Chooser randomChooser = new Chooser() {
        final Random random = new Random(0);
        public <T> T choose(List<T> list) {
            return list.get(random.nextInt(list.size()));
        }
    };

    @Test
    public void testAddAllLeft() {
        genericTest(leftChooser);
    }

    @Test
    public void testAddAllRight() {
        genericTest(rightChooser);
    }

    @Test
    public void testRandomized() {
        genericTest(randomChooser);
    }

    private <T> void genericTest(Chooser chooser) {
        final int total = 10240;
        List<Node<Integer>> elements = new ArrayList<Node<Integer>>(total);

        BenderList<Integer> list = BenderList.create();
        elements.add(list.base());
        for (int i = 0; i < total; i++) {
            Node<Integer> left = chooser.choose(elements);

            Node<Integer> newElement = list.addAfter(left, i);
            elements.add(newElement);
        }
        assertEquals((long)total, list.size());
        assertAscending(list);

        for (Node<Integer> node = list.base().next(); node != list.base(); node = node.next()) {
            assertTrue(node.previous().precedes(node));
        }
    }

    @Test
    public void testAddAfterDeleted() {
        BenderList<Integer> list = BenderList.create();

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSize() {
        BenderList list = BenderList.create();
        assertEquals(0, list.size());

        Node n1 = list.addAfter(list.base(), null);
        assertEquals(1, list.size());

        Node n2 = list.addAfter(n1, 10);
        assertEquals(2, list.size());

        assertSame(n1, list.base().next());
        assertSame(n2, n1.next());
        assertSame(n2, list.base().previous());
        assertSame(n1, n2.previous());

        assertTrue(list.delete(n1));
        assertSame(n2, list.base().next());
        assertSame(n2, list.base().previous());
        assertSame(list.base(), n2.next());
        assertSame(list.base(), n2.previous());
        assertEquals(1, list.size());

        assertTrue(list.delete(list.base().next()));
        assertEquals(0, list.size());

        assertSame(list.base(), list.base().next());
        assertSame(list.base(), list.base().previous());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDelete() {
        BenderList list = BenderList.create();
        Node node = list.addAfter(list.base(), null);

        assertTrue(node.isValid());
        assertTrue(list.delete(node));

        assertFalse(node.isValid());
        assertFalse(list.delete(node));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCannotDeleteBase() {
        BenderList list = BenderList.create();
        assertFalse(list.delete(list.base()));
    }

    @Test
    public void simpleTest() {
        BenderList<Void> list = BenderList.create();

        BenderList.Node<Void> b = (BenderList.Node<Void>)list.base();
        BenderList.Node<Void> n0 = (BenderList.Node<Void>)list.addAfter(b, null);
        BenderList.Node<Void> n1 = (BenderList.Node<Void>)list.addAfter(n0, null);
        BenderList.Node<Void> n2 = (BenderList.Node<Void>)list.addAfter(n1, null);
        BenderList.Node<Void> n3 = (BenderList.Node<Void>)list.addAfter(n2, null);

        n0.tag = -1;
        n1.tag = 0;
        n2.tag = 1;
        n3.tag = 3;

        Node<Void> n4 = (Node<Void>)list.addAfter(n1, null);
        assertAscending(list);
        assertPrecedes(b, n0);
        assertPrecedes(n0, n1);
        assertPrecedes(n1, n4);
        assertPrecedes(n4, n2);
        assertPrecedes(n2, n3);
    }

    private static void assertAscending(BenderList<?> list) {
        BenderList.Node<?> node = (BenderList.Node<?>)list.base().next();
        long last = Long.MIN_VALUE;
        while (node != list.base()) {
            assertTrue(last < node.tag);
            node = (BenderList.Node<?>)node.next();
        }
    }

    @SuppressWarnings("unchecked")
    private static void assertPrecedes(Node n1, Node n2) {
        assertTrue(n1.precedes(n2));
        assertFalse(n2.precedes(n1));
    }
}