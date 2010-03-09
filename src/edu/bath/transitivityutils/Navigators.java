package edu.bath.transitivityutils;

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

    public static <E> Navigator<E> invert(Navigator<E> navigator) {
        SetMultimap<E, E> relationships = HashMultimap.create();
        for (E subject : navigator.domain()) {
            for (E object : navigator.related(subject)) {
                relationships.put(object, subject);
            }
        }
        return forMultimap(relationships);
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
