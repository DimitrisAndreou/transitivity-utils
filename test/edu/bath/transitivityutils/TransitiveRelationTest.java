package edu.bath.transitivityutils;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public class TransitiveRelationTest {

    TransitiveRelation<Object> r;

    @After
    public void tearDown() {
        r = null;
    }

    @Test
    public void testReflexivityForUnknownNodes() {
        r = TransitiveRelation.create();
        assertRelations(
                1, 1, 
                2, 2);
        
        assertDirectRelations(
                3, 3,
                4, 4);
    }

    @Test
    public void testAcyclic1() {
        r = TransitiveRelation.create();
        r.relate(2, 3);
        r.relate(2, 4);
        r.relate(0, 1);
        r.relate(1, 2);

        assertRelations(
                0, 1,
                0, 2,
                0, 3,
                0, 4,
                1, 2,
                1, 3,
                1, 4,
                2, 3,
                2, 4);
    }

    @Test
    public void testAcyclic2() { //same, just other order of insertion
        r = TransitiveRelation.create();
        r.relate(2, 3);
        r.relate(2, 4);
        r.relate(1, 2);
        r.relate(0, 1);

        assertRelations(
                0, 1,
                0, 2,
                0, 3,
                0, 4,
                1, 2,
                1, 3,
                1, 4,
                2, 3,
                2, 4);
    }

    @Test
    public void testCyclic() {
        r = TransitiveRelation.create();
        r.relate(2, 3);
        r.relate(2, 4);
        r.relate(1, 2);
        r.relate(0, 1);
        r.relate(2, 0);

        assertRelations(
                0, 1,
                0, 2,
                0, 3,
                0, 4,
                1, 0,
                1, 2,
                1, 3,
                1, 4,
                2, 0,
                2, 1,
                2, 3,
                2, 4);
    }

    @Test
    public void testAcyclic3() {
        r = TransitiveRelation.create();
        r.relate(1, 2);
        r.relate(0, 1);
        r.relate(4, 5);
        r.relate(3, 4);
        r.relate(0, 4);
        r.relate(4, 2);

        assertRelations(
                0, 1,
                0, 2,
                0, 4,
                0, 5,
                1, 2,
                3, 2,
                3, 4,
                3, 5,
                4, 2,
                4, 5);
    }

    @Test
    public void testCyclesDoNotRemoveVitalEdgesAsRedundant() {
        r = TransitiveRelation.create();
        r.relate(22, 33);
        r.relate(11, 22);
        r.relate(22, 11);
        r.relate(44, 55);
        r.relate(44, 11);

        assertRelations(11, 22, 22, 33, 11, 33, 22, 11,
                44, 55,
                44, 11,
                44, 22,
                44, 33);
    }

    @Test
    public void testSimple() {
        r = TransitiveRelation.create();
        r.relate(0, 1);
        r.relate(1, 0);
        r.relate(0, 2);

        assertRelations(
                0, 1,
                0, 2,
                1, 0,
                1, 2);
    }

    @Test
    public void testSimple2() {
        r = TransitiveRelation.create();
        r.relate(0, 1);
        r.relate(1, 0);
        r.relate(0, 2);
        r.relate(1, 2);

        assertRelations(
                0, 1,
                0, 2,
                1, 0,
                1, 2);

        r.relate(4, 5);
        r.relate(4, 1);

        assertRelations(
                0, 1,
                0, 2,
                1, 0,
                1, 2,
                4, 0,
                4, 1,
                4, 2,
                4, 5);
    }

    @Test
    public void testBottomUp() {
        r = TransitiveRelation.create();
        r.relate(1, 2);
        r.relate(2, 3);
        r.relate(3, 4);

        assertRelations(
                1, 2,
                1, 3,
                2, 3);
    }

    @Test
    public void testDirectlyRelated() {
        r = TransitiveRelation.create();

        r.relate(1, 2);
        r.relate(2, 3);
        r.relate(3, 4);
        r.relate(3, 5);

        assertDirectRelations(
                1, 2,
                2, 3,
                3, 4,
                3, 5);
    }

    @Test
    public void testDirectlyRelatedWith() {
        r = TransitiveRelation.create();

        r.relate(1, 2);
        r.relate(2, 3);
        r.relate(3, 4);
        r.relate(3, 5);

        assertEquals(ImmutableSet.of(), ImmutableSet.copyOf(r.directlyRelatedWith(0)));
        assertEquals(ImmutableSet.of(2), ImmutableSet.copyOf(r.directlyRelatedWith(1)));
        assertEquals(ImmutableSet.of(4, 5), ImmutableSet.copyOf(r.directlyRelatedWith(3)));
        assertEquals(ImmutableSet.of(), ImmutableSet.copyOf(r.directlyRelatedWith(4)));
        assertEquals(ImmutableSet.of(), ImmutableSet.copyOf(r.directlyRelatedWith(5)));
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testDirectlyRelatedWith_Unmodifiable1() {
        r = TransitiveRelation.create();
        r.directlyRelatedWith(0).add(null);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testDirectlyRelatedWith_Unmodifiable2() {
        r = TransitiveRelation.create();
        r.relate(0, 1);
        r.directlyRelatedWith(0).add(null);
    }

    @Test
    public void testReflexiveEdgesIgnored() {
        r = TransitiveRelation.create();
        r.relate(0, 0);
        assertTrue(r.directlyRelatedWith(0).isEmpty());
    }

    private void assertRelations(Object... pairs) {
        Set<Entry<Object, Object>> relations = Sets.newHashSet();
        Set<Object> domain = Sets.newHashSet();
        for (int i = 0; i < pairs.length; i += 2) {
            relations.add(Maps.immutableEntry(pairs[i], pairs[i + 1]));
            domain.add(pairs[i]);
            domain.add(pairs[i + 1]);
        }

        for (Object o1 : domain) {
            for (Object o2 : domain) {
                if (o1 == o2) {
                    assertTrue(r.areRelated(o1, o2));
                } else {
                    Entry<Object, Object> entry = Maps.immutableEntry(o1, o2);
                    assertEquals(entry.toString(), relations.contains(entry), r.areRelated(o1, o2));
                }
            }
        }
    }

    private void assertDirectRelations(Object... pairs) {
        Set<Entry<Object, Object>> relations = Sets.newHashSet();
        Set<Object> domain = Sets.newHashSet();
        for (int i = 0; i < pairs.length; i += 2) {
            relations.add(Maps.immutableEntry(pairs[i], pairs[i + 1]));
            domain.add(pairs[i]);
            domain.add(pairs[i + 1]);
        }

        for (Object o1 : domain) {
            for (Object o2 : domain) {
                if (o1 == o2) {
                    assertTrue(r.areRelated(o1, o2));
                } else {
                    Entry<Object, Object> entry = Maps.immutableEntry(o1, o2);
                    assertEquals(entry.toString(), relations.contains(entry), r.areDirectlyRelated(o1, o2));
                }
            }
        }
    }
}