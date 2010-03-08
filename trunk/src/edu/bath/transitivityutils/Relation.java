package edu.bath.transitivityutils;

/**
 * A binary relation.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public interface Relation<E> {
    /** optional operation */
    void relate(E subject, E object);
    
    boolean areRelated(E subject, E object);
}
