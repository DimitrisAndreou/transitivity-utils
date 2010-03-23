package edu.bath.transitivityutils;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Provides static utility methods for creating and working with {@link
 * Navigator} instances.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public final class Navigators {
    private Navigators() { }

    /**
     * Creates a {@code Navigator} <em>view</em> of the supplied {@link SetMultimap} instance.
     * The {@code domain()} of the created navigator will be the {@code keySet()} of the multimap,
     * whereas the navigator's {@code related(element)} invocations will be translated
     * to {@code multimap.get(element)} invocations.
     *
     * <p>The returned navigator will be serializable if the specified multimap is serializable.
     *
     * @param multimap the backing multimap of the returned navigator view
     */
    public static <E> Navigator<E> forMultimap(SetMultimap<E, E> multimap) {
        return new MultimapNavigator<E>(Preconditions.checkNotNull(multimap));
    }

    //view
    //serializable if domain and navigationFunction is
    /**
     * Creates a {@code Navigator} <em>view</em> of the supplied domain and function.
     * The specified function is used to implement the returned navigator's {@code related(element)}
     * invocations. It must return an empty set if applied to an element not in the supplied domain.
     *
     * <p>The returned navigator will be serializable if the specified set and function are serializable.
     *
     * @param domain the domain of the returned navigator
     * @param navigationFunction the function that will provide the implementation
     * of navigator's {@linkplain Navigator#related(Object) related(Object)} method
     */
    public static <E> Navigator<E> forFunction(Set<E> domain,
            Function<? super E, ? extends Set<E>> navigationFunction) {
        return new FunctionNavigator<E>(Preconditions.checkNotNull(domain),
                Preconditions.checkNotNull(navigationFunction));
    }

    private static class FunctionNavigator<E> implements Navigator<E>, Serializable {
        private final Set<E> domain;
        private final Function<? super E, ? extends Set<E>> navigationFunction;

        private static final long serialVersionUID = 6024090827962229701L;

        FunctionNavigator(Set<E> domain,
                Function<? super E, ? extends Set<E>> navigationFunction) {
            this.domain = domain;
            this.navigationFunction = navigationFunction;
        }

        public Set<E> related(E subjectValue) {
            return navigationFunction.apply(subjectValue);
        }

        public Set<E> domain() {
            return domain;
        }
    }
    
    private static class MultimapNavigator<E> implements Navigator<E>, Serializable {
        private final SetMultimap<E, E> multimap;

        private static final long serialVersionUID = 8800521367524594039L;

        MultimapNavigator(SetMultimap<E, E> multimap) {
            this.multimap = multimap;
        }

        public Set<E> related(E subjectValue) {
            return multimap.get(subjectValue);
        }

        public Set<E> domain() {
            return multimap.keySet();
        }
    }

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
        return closureOfMany(navigator, Collections.singleton(object));
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

    static <E> List<E> topologicalOrder(Navigator<E> acyclicNavigator) {
        final List<E> topologicalOrder = Lists.newArrayListWithCapacity(acyclicNavigator.domain().size());
        new Dfs<E>(acyclicNavigator) {
            @Override
            protected void postVisit(E value) {
                topologicalOrder.add(value);
            }
        }.execute(false); //false: not allowing cycles
        
        return topologicalOrder;
    }

    /**
     * Returns a <b>view</b> of the difference of two {@code Navigator}s. If
     * a relationship is contained in both {@code nav1} and {@code nav2}, or in {@code nav2}
     * only, it is ignored by the returned navigator. Its
     * domain contains only those elements of {@code nav1}'s domain
     * which contain relationships not found in {@code nav2}.
     */
    public static <E> Navigator<E> difference(Navigator<E> nav1, Navigator<E> nav2) {
        return new DifferenceNavigator<E>(Preconditions.checkNotNull(nav1), Preconditions.checkNotNull(nav2));
    }

    private static abstract class NavigatorWithPredicate<E> implements Navigator<E> {
        protected final Predicate<E> onlyElementsWithRelations = new Predicate<E>() {
            public boolean apply(E input) {
                return !related(input).isEmpty();
            }
        };
    }

    private static class DifferenceNavigator<E> extends NavigatorWithPredicate<E> {
        private final Navigator<E> positive;
        private final Navigator<E> negative;

        DifferenceNavigator(Navigator<E> positive, Navigator<E> negative) {
            this.positive = positive;
            this.negative = negative;
        }

        public Set<E> domain() {
            return Sets.filter(positive.domain(), onlyElementsWithRelations);
        }

        public Set<E> related(E e) {
            return Sets.difference(positive.related(e), negative.related(e));
        }
    }

    /**
     * Returns a <b>view</b> of the union of two {@code Navigator}s. If
     * a relationship is contained in either {@code nav1} or {@code nav2},
     * it is included in the returned navigator. Its
     * domain is the union of {@code nav1}'s and {@code nav2}'s domains.
     */
    public static <E> Navigator<E> union(Navigator<E> nav1, Navigator<E> nav2) {
        return new UnionNavigator<E>(Preconditions.checkNotNull(nav1), Preconditions.checkNotNull(nav2));
    }

    private static class UnionNavigator<E> implements Navigator<E> {
        private final Navigator<E> nav1;
        private final Navigator<E> nav2;

        UnionNavigator(Navigator<E> nav1, Navigator<E> nav2) {
            this.nav1 = nav1;
            this.nav2 = nav2;
        }

        public Set<E> domain() {
            return Sets.union(nav1.domain(), nav2.domain());
        }

        public Set<E> related(E e) {
            return Sets.union(nav1.related(e), nav2.related(e));
        }
    }

    /**
     * Returns a <b>view</b> of the intersection of two {@code Navigator}s. If
     * a relationship is contained in both {@code nav1} and {@code nav2},
     * it is included in the returned navigator. Its domain contains only the elements
     * in the intersection of {@code nav1}'s and {@code nav2}'s domains that
     * have relationships in both navigators.
     */
    public static <E> Navigator<E> intersection(Navigator<E> nav1, Navigator<E> nav2) {
        return new IntersectionNavigator<E>(Preconditions.checkNotNull(nav1), Preconditions.checkNotNull(nav2));
    }

    private static class IntersectionNavigator<E> extends NavigatorWithPredicate<E> {
        private final Navigator<E> nav1;
        private final Navigator<E> nav2;

        IntersectionNavigator(Navigator<E> nav1, Navigator<E> nav2) {
            this.nav1 = nav1;
            this.nav2 = nav2;
        }

        public Set<E> domain() {
            return Sets.filter(Sets.intersection(nav1.domain(), nav2.domain()), onlyElementsWithRelations);
        }

        public Set<E> related(E e) {
            return Sets.intersection(nav1.related(e), nav2.related(e));
        }
    }
}
