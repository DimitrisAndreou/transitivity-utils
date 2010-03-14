package edu.bath.transitivityutils;

import edu.bath.transitivityutils.OrderList.Node;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MergingIntervalSetTest {
    OrderList<Integer> list;
    List<Node<Integer>> nodes;
    MergingIntervalSet set;

    private static final Field arrayField;
    static {
        try {
            arrayField = MergingIntervalSet.class.getDeclaredField("array");
            arrayField.setAccessible(true);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Before
    public void init() {
        list = OrderList.create();
        nodes = new ArrayList<Node<Integer>>();
        for (int i = 0; i < 16; i++) {
            nodes.add(list.addAfter(list.base().previous(), i));
        }
        set = new MergingIntervalSet();
    }

    @After
    public void tearDown() {
        list = null;
        nodes = null;
        set = null;
    }

    @Test
    public void testInitial() {
        set.addInterval(nodes.get(3), nodes.get(4));
        assertContains(nodes.get(3), nodes.get(4));
        assertEquals(2, set.size());
    }

    @Test
    public void testAddIndependentLeft() {
        set.addInterval(nodes.get(3), nodes.get(4));
        set.addInterval(nodes.get(1), nodes.get(2));

        assertContains(nodes.get(1), nodes.get(2));
        assertContains(nodes.get(3), nodes.get(4));
        assertEquals(4, set.size());
    }

    @Test
    public void testAddIndependentRight() {
        set.addInterval(nodes.get(3), nodes.get(4));
        set.addInterval(nodes.get(5), nodes.get(6));

        assertContains(nodes.get(3), nodes.get(4));
        assertContains(nodes.get(5), nodes.get(6));
        assertEquals(4, set.size());
    }

    @Test
    public void addSubsumingOfOneInterval() {
        set.addInterval(nodes.get(3), nodes.get(4));
        set.addInterval(nodes.get(2), nodes.get(5));
        assertContains(nodes.get(2), nodes.get(5));
        assertEquals(2, set.size());
    }

    @Test
    public void addSubsumingOfMoreThanOne_Left() {
        set.addInterval(nodes.get(3), nodes.get(4));
        set.addInterval(nodes.get(1), nodes.get(2));
        set.addInterval(nodes.get(0), nodes.get(5));

        assertContains(nodes.get(0), nodes.get(5));
        assertEquals(2, set.size());
    }

    @Test
    public void addSubsumingOfMoreThanOne_Right() {
        set.addInterval(nodes.get(3), nodes.get(4));
        set.addInterval(nodes.get(5), nodes.get(6));
        set.addInterval(nodes.get(2), nodes.get(7));

        assertContains(nodes.get(2), nodes.get(7));
        assertEquals(2, set.size());
    }

    @Test
    public void testOverlapping_Left() {
        set.addInterval(nodes.get(3), nodes.get(4));
        set.addInterval(nodes.get(1), nodes.get(2));
        set.addInterval(nodes.get(5), nodes.get(6));

        Node<Integer> between = list.addAfter(nodes.get(3), 34);
        set.addInterval(between, nodes.get(7));
        assertContains(nodes.get(1), nodes.get(2));
        assertContains(nodes.get(3), nodes.get(7));
        assertEquals(4, set.size());
    }

    @Test
    public void testOverlapping_Right() {
        set.addInterval(nodes.get(3), nodes.get(4));
        set.addInterval(nodes.get(1), nodes.get(2));
        set.addInterval(nodes.get(5), nodes.get(6));

        Node<Integer> between = list.addAfter(nodes.get(3), 34);
        set.addInterval(nodes.get(0), between);
        assertContains(nodes.get(0), nodes.get(4));
        assertContains(nodes.get(5), nodes.get(6));
        assertEquals(4, set.size());
    }

    @Test
    public void testOverlapping_Both() {
        set.addInterval(nodes.get(0), nodes.get(1));
        set.addInterval(nodes.get(2), nodes.get(3));
        set.addInterval(nodes.get(4), nodes.get(5));
        set.addInterval(nodes.get(6), nodes.get(7));

        assertEquals(8, set.size());

        set.addInterval(list.addAfter(nodes.get(2), 23), list.addAfter(nodes.get(4), 45));
        assertContains(nodes.get(2), nodes.get(5));
        assertEquals(6, set.size());

        set.addInterval(list.addAfter(nodes.get(0), 01), list.addAfter(nodes.get(6), 67));
        assertContains(nodes.get(0), nodes.get(7));
        assertEquals(2, set.size());
    }

    @Test
    public void testOverlapping_Bigger() {
        set.addInterval(nodes.get(1), nodes.get(2));
        set.addInterval(nodes.get(3), nodes.get(4));
        set.addInterval(nodes.get(5), nodes.get(6));

        set.addInterval(nodes.get(0), nodes.get(7));
        assertContains(nodes.get(0), nodes.get(7));
        assertEquals(2, set.size());
    }

    @Test(expected=RuntimeException.class)
    public void testIllegalArguments() {
        set.addInterval(nodes.get(1), nodes.get(2));
        set.addInterval(nodes.get(4), nodes.get(3));
    }

    @Test
    public void testReaddingSameElements() {
        set.addInterval(nodes.get(1), nodes.get(2));
        set.addInterval(nodes.get(1), nodes.get(2));

        assertContains(nodes.get(1), nodes.get(2));
        assertEquals(2, set.size());
    }

    @Test
    public void testReaddingOneElements_Left() {
        set.addInterval(nodes.get(1), nodes.get(2));
        set.addInterval(nodes.get(1), nodes.get(3));

        assertContains(nodes.get(1), nodes.get(3));
        assertEquals(2, set.size());
    }

    @Test
    public void testReaddingOneElements_Right() {
        set.addInterval(nodes.get(2), nodes.get(3));
        set.addInterval(nodes.get(1), nodes.get(3));

        assertContains(nodes.get(1), nodes.get(3));
        assertEquals(2, set.size());
    }

    @Test
    public void testResizingBehavior1() {
        set.addInterval(nodes.get(1), nodes.get(2));
        assertEquals(2, capacityOf(set));

        set.addInterval(nodes.get(3), nodes.get(4));
        assertEquals(4, capacityOf(set));

        set.addInterval(nodes.get(5), nodes.get(6));
        assertEquals(8, capacityOf(set));

        set.addInterval(nodes.get(7), nodes.get(8));
        assertEquals(8, capacityOf(set));

        set.addInterval(nodes.get(9), nodes.get(10));
        assertEquals(16, capacityOf(set));

        set.addInterval(nodes.get(11), nodes.get(12));
        assertEquals(16, capacityOf(set));

        //this steps makes size 2
        set.addInterval(nodes.get(0), nodes.get(13));
        assertEquals(2, set.size());
        assertEquals(2, capacityOf(set));

        set.addInterval(nodes.get(14), nodes.get(15));
        assertEquals(4, capacityOf(set));
    }

    private int capacityOf(MergingIntervalSet mis) {
        try {
            return ((Node<?>[])arrayField.get(mis)).length;
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private void assertContains(Node<Integer> pre, Node<Integer> post) {
        assertTrue(set.contains(pre));
        assertTrue(set.contains(post));

        Node<Integer> tmp = list.addAfter(pre.previous(), -1);
        assertFalse(set.contains(tmp));
        list.delete(tmp);

        tmp = list.addAfter(post, -1);
        assertFalse(set.contains(tmp));
        list.delete(tmp);
    }
}