package edu.bath.transitivityutils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import java.util.Collection;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public final class Navigators {
    private Navigators() { }

    public static <E> Navigator<E> forMultimap(Multimap<E, E> multimap) {
        return new MultimapNavigator<E>(Preconditions.checkNotNull(multimap));
    }

    private static class MultimapNavigator<E> implements Navigator<E> {
        private final Multimap<E, E> multimap;

        MultimapNavigator(Multimap<E, E> multimap) {
            this.multimap = multimap;
        }

        public Collection<E> related(E subjectValue) {
            return multimap.get(subjectValue);
        }

        public Collection<E> domain() {
            return multimap.keySet();
        }
    }
}
