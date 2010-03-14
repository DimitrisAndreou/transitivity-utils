package edu.bath.transitivityutils;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Provides static utility methods for creating and working with {@link
 * Relation} instances.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public final class Relations {
    private Relations() { }

    /**
     * Returns the transitive closure of an object (which always includes the object itself),
     * which is the set of objects that are (by any number of steps) reachable from
     * the specified object, using the supplied navigator.
     *
     * @param navigator the navigator to be used to compute the transitive closure of an element
     * @param object an object (defined in the {@linkplain Navigator#domain() domain} of the navigator)
     * @return the transitive closure of the element (which includes the element itself)
     */
    public static <E> Set<E> closure(Navigator<E> navigator, E object) {
        return Relations.closureOfMany(navigator, Collections.singleton(object));
    }

    /**
     * Returns the unon of the transitive closures of some objects (which always includes the objects themselves),
     * which is the set of objects that are (by any number of steps) reachable from
     * any of the specified objects, using the supplied navigator.
     *
     * <p>When the transitive
     * closures of the objects are expected to overlap, this method is likely to be more efficient than
     * computing separately the transitive closure of each object and then computing the union of them.
     *
     * @param navigator the navigator to be used to compute the transitive closure of an element
     * @param objects some objects (defined in the {@linkplain Navigator#domain() domain} of the navigator)
     * @return the transitive closure of the element (which includes the element itself)
     */
    public static <E> Set<E> closureOfMany(Navigator<E> navigator, Iterable<? extends E> objects) {
        Set<E> closure = Sets.newHashSet();
        Iterator<? extends E> toExplore = objects.iterator();

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

    /**
     * Creates a new, empty, transitive relation.
     *
     * <p>The created {@code TransitiveRelation} is serializable, as long as the objects in the relation
     * are themselves serializable.
     */
    public static <E> TransitiveRelation<E> newTransitiveRelation() {
        return new DefaultTransitiveRelation<E>();
    }

    /**
     * Creates a new, empty, bidirectional transitive relation.
     *
     * <p>The created {@code TransitiveBiRelation} is serializable, as long as the objects in the relation
     * are themselves serializable.
     */
    public static <E> TransitiveBiRelation<E> newTransitiveBiRelation() {
        return new DefaultTransitiveBiRelation<E>();
    }

    /**
     * Merges into a {@link TransitiveRelation} all particular relationships found in the specified
     * navigator.
     *
     * <p>The relationships of a navigator are found by iterating its {@link Navigator#domain() domain},
     * and for each object in that, finding all objects {@link Navigator#related(Object) related} to it.
     *
     * @param relation the transitive relation into which to merge the relationships of the navigator
     * @param navigator a navigator
     */
    public static <E> void merge(TransitiveRelation<? super E> relation, Navigator<E> navigator) {
        //to be optimized later, much better algorithms (as per the labeling they produce) exist
        //i.e., optimal tree cover, longest path
        for (E subject : navigator.domain()) {
            for (E object : navigator.related(subject)) {
                relation.relate(subject, object);
            }
        }
    }
}
