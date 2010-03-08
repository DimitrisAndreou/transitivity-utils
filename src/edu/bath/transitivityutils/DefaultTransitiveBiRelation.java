package edu.bath.transitivityutils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
class DefaultTransitiveBiRelation<E> implements TransitiveBiRelation<E> {
    private final Multimap<E, E> inverseEdges = HashMultimap.create();
    private final DefaultTransitiveRelation<E> relation = new DefaultTransitiveRelation<E>() {
        @Override
        void recordRelationship(E subjectValue, E objectValue) {
            inverseEdges.put(objectValue, subjectValue);
        }
    };

    private final Navigator<E> inverseRelation = Navigators.forMultimap(inverseEdges);

    private final TransitiveBiRelation<E> inverse = new TransitiveBiRelation<E>() {
        public Navigator<E> direct() {
            return inverseRelation;
        }

        public void relate(E subject, E object) {
            relation.relate(object, subject);
        }

        public boolean areRelated(E subject, E object) {
            return relation.areRelated(object, subject);
        }

        public TransitiveBiRelation<E> inverse() {
            return DefaultTransitiveBiRelation.this;
        }
    };

    DefaultTransitiveBiRelation() { }

    public void relate(E subjectValue, E objectValue) {
        relation.relate(subjectValue, objectValue);
    }

    public boolean areRelated(E subjectValue, E objectValue) {
        return relation.areRelated(subjectValue, objectValue);
    }

    public Navigator<E> direct() {
        return relation.direct();
    }

    public TransitiveBiRelation<E> inverse() {
        return inverse;
    }
}
