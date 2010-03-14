package edu.bath.transitivityutils;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.io.Serializable;
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
     * invocations.
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
}