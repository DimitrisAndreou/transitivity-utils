package edu.bath.transitivityutils;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public class NavigatorsTest {
    @Test
    public void testBasic() {
        Navigator<String> navigator = Navigators.forMultimap(ImmutableSetMultimap.of("a", "a1", "a", "a2", "b", "b"));
        assertEquals(ImmutableSet.of("a1", "a2"), ImmutableSet.copyOf(navigator.related("a")));
        assertEquals(ImmutableSet.of("b"), ImmutableSet.copyOf(navigator.related("b")));
        assertEquals(ImmutableSet.of(), ImmutableSet.copyOf(navigator.related("c")));
    }

    @Test
    public void testDomain() {
        Navigator<String> navigator = Navigators.forMultimap(ImmutableSetMultimap.of("a", "a1", "a", "a2", "b", "b"));

        assertEquals(ImmutableSet.of("a", "b"), ImmutableSet.copyOf(navigator.domain()));
    }

    @Test
    public void testInvert() {
        Navigator<String> navigator = Navigators.invert(
                Navigators.forMultimap(ImmutableSetMultimap.of("a", "a1", "a", "a2", "b", "b")));

        assertEquals(ImmutableSet.of("a1", "a2", "b"), ImmutableSet.copyOf(navigator.domain()));

        assertEquals(ImmutableSet.of("a"), ImmutableSet.copyOf(navigator.related("a1")));
        assertEquals(ImmutableSet.of("a"), ImmutableSet.copyOf(navigator.related("a2")));
        assertEquals(ImmutableSet.of("b"), ImmutableSet.copyOf(navigator.related("b")));
        assertEquals(ImmutableSet.of(), ImmutableSet.copyOf(navigator.related("a")));
    }

    @Test
    public void testSerializable() {
        Navigator<String> navigator = SerializationUtils.serializedCopy(Navigators.forMultimap(ImmutableSetMultimap.of("a", "a1", "a", "a2", "b", "b")));

        assertEquals(ImmutableSet.of("a1", "a2"), ImmutableSet.copyOf(navigator.related("a")));
        assertEquals(ImmutableSet.of("b"), ImmutableSet.copyOf(navigator.related("b")));
        assertEquals(ImmutableSet.of(), ImmutableSet.copyOf(navigator.related("c")));

        
        navigator = SerializationUtils.serializedCopy(Navigators.invert(
                Navigators.forMultimap(ImmutableSetMultimap.of("a", "a1", "a", "a2", "b", "b"))));

        assertEquals(ImmutableSet.of("a1", "a2", "b"), ImmutableSet.copyOf(navigator.domain()));

        assertEquals(ImmutableSet.of("a"), ImmutableSet.copyOf(navigator.related("a1")));
        assertEquals(ImmutableSet.of("a"), ImmutableSet.copyOf(navigator.related("a2")));
        assertEquals(ImmutableSet.of("b"), ImmutableSet.copyOf(navigator.related("b")));
        assertEquals(ImmutableSet.of(), ImmutableSet.copyOf(navigator.related("a")));
    }
}