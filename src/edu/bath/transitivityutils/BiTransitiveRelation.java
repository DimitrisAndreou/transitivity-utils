package edu.bath.transitivityutils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public class BiTransitiveRelation<E> {
    private final Multimap<E, E> inverseEdges = HashMultimap.create();
    private final TransitiveRelation<E> relation = new TransitiveRelation<E>() {
        @Override
        void recordRelationship(E subjectValue, E objectValue) {
            inverseEdges.put(objectValue, subjectValue);
        }
    };
    private final Navigator<E> inverseRelation = Navigators.forMultimap(inverseEdges);

    public void relate(E subjectValue, E objectValue) {
        relation.relate(subjectValue, objectValue);
    }

    public boolean areRelated(E subjectValue, E objectValue) {
        return relation.areRelated(subjectValue, objectValue);
    }

    public Navigator<E> inverse() {
        return inverseRelation;
    }
}
