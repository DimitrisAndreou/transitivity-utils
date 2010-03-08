package edu.bath.transitivityutils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public class NavigatorsTest {
    @Test public void testBasic() {
        Navigator<String> navigator = Navigators.forMultimap(ImmutableMultimap.of("a", "a1", "a", "a2", "b", "b"));
        assertEquals(ImmutableSet.of("a1", "a2"), ImmutableSet.copyOf(navigator.related("a")));
        assertEquals(ImmutableSet.of("b"), ImmutableSet.copyOf(navigator.related("b")));
        assertEquals(ImmutableSet.of(), ImmutableSet.copyOf(navigator.related("c")));
    }

    @Test public void testDomain() {
        Navigator<String> navigator = Navigators.forMultimap(ImmutableMultimap.of("a", "a1", "a", "a2", "b", "b"));

        assertEquals(ImmutableSet.of("a", "b"), ImmutableSet.copyOf(navigator.domain()));
    }
}