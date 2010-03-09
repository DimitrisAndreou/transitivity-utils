package edu.bath.transitivityutils;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public final class Relations {
    private Relations() { }

    /**
     * Returns the transitive closure of an element (which always includes the element itself).
     * The returned set can be modified freely without side-effects to this relation.
     *
     * @param e an element
     * @return the transitive closure of the element (which includes the element itself)
     */
    public static <E> Set<E> closure(Navigator<E> navigator, E e) {
        return Relations.closureOfMany(navigator, Collections.singleton(e));
    }

    /**
     * Returns the union of the transitive closure of some elements (which always includes the elements themselves).
     * The returned set can be modified freely without side-effects to this relation. When the transitive
     * closures of the elements are expected to overlap, this method is more efficient than
     * computing separately the transitive closure of each element and unioning them.
     *
     * @param elements some elements
     * @return the transitive closure of the elements (which includes the elements themselves)
     */
    public static <E> Set<E> closureOfMany(Navigator<E> navigator, Iterable<? extends E> elements) {
        Set<E> closure = Sets.newHashSet();
        Iterator<? extends E> toExplore = elements.iterator();

        while (toExplore.hasNext()) {
            E next = toExplore.next();
            if (closure.contains(next)) {
                continue;
            }
            closure.add(next);
            toExplore = Iterators.concat(navigator.related(next).iterator(), toExplore); //adding the directly related elements
        }
        return closure;
    }

    //the returned object is serializable
    public static <E> TransitiveRelation<E> newTransitiveRelation() {
        return new DefaultTransitiveRelation<E>();
    }

    //the returned object is serializable
    public static <E> TransitiveBiRelation<E> newTransitiveBiRelation() {
        return new DefaultTransitiveBiRelation<E>();
    }
}
