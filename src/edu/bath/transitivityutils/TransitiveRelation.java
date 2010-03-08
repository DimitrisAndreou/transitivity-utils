package edu.bath.transitivityutils;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public interface TransitiveRelation<E> extends Relation<E> {
    Navigator<E> direct();
}
