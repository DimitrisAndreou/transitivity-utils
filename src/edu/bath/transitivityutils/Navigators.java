package edu.bath.transitivityutils;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public final class Navigators {
    private Navigators() { }

    public static <E> Navigator<E> forMultimap(SetMultimap<E, E> multimap) {
        return new MultimapNavigator<E>(Preconditions.checkNotNull(multimap));
    }

    //view
    //serializable if domain and navigationFunction is
    public static <E> Navigator<E> forFunction(Set<E> domain,
            Function<? super E, ? extends Set<E>> navigationFunction) {
        return new FunctionNavigator<E>(Preconditions.checkNotNull(domain),
                Preconditions.checkNotNull(navigationFunction));
    }

    public static <E> Navigator<E> invert(Navigator<E> navigator) {
        SetMultimap<E, E> relationships = HashMultimap.create();
        for (E subject : navigator.domain()) {
            for (E object : navigator.related(subject)) {
                relationships.put(object, subject);
            }
        }
        return forMultimap(relationships);
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
