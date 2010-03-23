package edu.bath.transitivityutils;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import java.io.Serializable;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public class NavigatorsTest {
    @Test
    public void testForMultimap_related() {
        Navigator<String> navigator = Navigators.forMultimap(ImmutableSetMultimap.of("a", "a1", "a", "a2", "b", "b"));
        assertEquals(ImmutableSet.of("a1", "a2"), ImmutableSet.copyOf(navigator.related("a")));
        assertEquals(ImmutableSet.of("b"), ImmutableSet.copyOf(navigator.related("b")));
        assertEquals(ImmutableSet.of(), ImmutableSet.copyOf(navigator.related("c")));
    }

    @Test
    public void testForMultimap_domain() {
        Navigator<String> navigator = Navigators.forMultimap(ImmutableSetMultimap.of("a", "a1", "a", "a2", "b", "b"));

        assertEquals(ImmutableSet.of("a", "b"), ImmutableSet.copyOf(navigator.domain()));
    }

    @Test
    public void testForFunction() {
        Navigator<Integer> navigator = Navigators.forFunction(ImmutableSet.of(1, 2, 3),
            new Function<Integer, Set<Integer>>() {
                public Set<Integer> apply(Integer value) {
                    return ImmutableSet.of(value * 2, value + 1);
                }
        });
        assertEquals(ImmutableSet.of(1, 2, 3), ImmutableSet.copyOf(navigator.domain()));
        assertEquals(ImmutableSet.of(2, 2), ImmutableSet.copyOf(navigator.related(1)));
        assertEquals(ImmutableSet.of(4, 3), ImmutableSet.copyOf(navigator.related(2)));
        assertEquals(ImmutableSet.of(6, 4), ImmutableSet.copyOf(navigator.related(3)));
    }

    @Test
    public void testForFunction_Serializable() {
        Navigator<Integer> navigator = SerializationUtils.serializedCopy(
                Navigators.forFunction(ImmutableSet.of(1, 2, 3), new MyFun()));

        assertEquals(ImmutableSet.of(1, 2, 3), ImmutableSet.copyOf(navigator.domain()));
        assertEquals(ImmutableSet.of(2, 2), ImmutableSet.copyOf(navigator.related(1)));
        assertEquals(ImmutableSet.of(4, 3), ImmutableSet.copyOf(navigator.related(2)));
        assertEquals(ImmutableSet.of(6, 4), ImmutableSet.copyOf(navigator.related(3)));
   }
    private static class MyFun implements Function<Integer, Set<Integer>>, Serializable {
        private static final long serialVersionUID = 0L;
        public Set<Integer> apply(Integer value) {
            return ImmutableSet.of(value * 2, value + 1);
        }
    };

    @Test
    public void testClosure() {
        TransitiveRelation<String> relation = Relations.newTransitiveRelation();
        relation.relate("A", "B");
        relation.relate("B", "C");
        Navigator<String> direct = relation.direct();

        assertEquals(ImmutableSet.of("A", "B", "C"), ImmutableSet.copyOf(Navigators.closure(direct, "A")));
        assertEquals(ImmutableSet.of("B", "C"), ImmutableSet.copyOf(Navigators.closure(direct, "B")));
        assertEquals(ImmutableSet.of("C"), ImmutableSet.copyOf(Navigators.closure(direct, "C")));
   }

    @Test
    public void testClosureOfMany() {
        TransitiveRelation<String> relation = Relations.newTransitiveRelation();
        relation.relate("A", "B");
        relation.relate("B", "C");
        relation.relate("D", "E");
        relation.relate("E", "F");
        Navigator<String> direct = relation.direct();

        assertEquals(ImmutableSet.of("A", "B", "C", "D", "E", "F"), ImmutableSet.copyOf(Navigators.closureOfMany(direct, ImmutableList.of("A", "D"))));
        assertEquals(ImmutableSet.of("B", "C", "E", "F"), ImmutableSet.copyOf(Navigators.closureOfMany(direct, ImmutableList.of("B", "E"))));
        assertEquals(ImmutableSet.of("C", "F"), ImmutableSet.copyOf(Navigators.closureOfMany(direct, ImmutableList.of("C", "F"))));
   }

    @Test
    public void testClosureAlwaysIncludesStart() {
        Navigator<String> navigator = Navigators.forMultimap(ImmutableSetMultimap.<String, String>of());
        assertEquals(ImmutableSet.of("A"), ImmutableSet.copyOf(Navigators.closure(navigator, "A")));
        assertEquals(ImmutableSet.of("A"), ImmutableSet.copyOf(Navigators.closureOfMany(navigator, ImmutableList.of("A"))));
    }

    @Test
    public void testDifference() {
         Navigator<String> nav1 = Navigators.forMultimap(ImmutableSetMultimap.of("a", "a1", "a", "a2", "b", "b"));
         Navigator<String> nav2 = Navigators.forMultimap(ImmutableSetMultimap.of("a", "a1", "c", "c2", "b", "b"));

         Navigator<String> diff = Navigators.difference(nav1, nav2);
        assertEquals(ImmutableSet.of("a"), ImmutableSet.copyOf(diff.domain()));

        assertEquals(ImmutableSet.of("a2"), ImmutableSet.copyOf(diff.related("a")));
        assertTrue(diff.related("b").isEmpty());
        assertTrue(diff.related("c").isEmpty());
   }

    @Test
    public void testUnion() {
         Navigator<String> nav1 = Navigators.forMultimap(ImmutableSetMultimap.of("a", "a1", "a", "a2", "b", "b"));
         Navigator<String> nav2 = Navigators.forMultimap(ImmutableSetMultimap.of("a", "a1", "c", "c2", "b", "b"));

         Navigator<String> union = Navigators.union(nav1, nav2);
        assertEquals(ImmutableSet.of("a", "b", "c"), ImmutableSet.copyOf(union.domain()));

        assertEquals(ImmutableSet.of("a1", "a2"), ImmutableSet.copyOf(union.related("a")));
        assertEquals(ImmutableSet.of("b"), ImmutableSet.copyOf(union.related("b")));
        assertEquals(ImmutableSet.of("c2"), ImmutableSet.copyOf(union.related("c")));
    }

    @Test
    public void testIntersection() {
         Navigator<String> nav1 = Navigators.forMultimap(ImmutableSetMultimap.of("a", "a1", "a", "a2", "b", "b"));
         Navigator<String> nav2 = Navigators.forMultimap(ImmutableSetMultimap.of("a", "a1", "c", "c2", "b", "b"));

         Navigator<String> intersection = Navigators.intersection(nav1, nav2);
        assertEquals(ImmutableSet.of("a", "b"), ImmutableSet.copyOf(intersection.domain()));

        assertEquals(ImmutableSet.of("a1"), ImmutableSet.copyOf(intersection.related("a")));
        assertEquals(ImmutableSet.of("b"), ImmutableSet.copyOf(intersection.related("b")));
        assertTrue(intersection.related("c").isEmpty());
   }
}