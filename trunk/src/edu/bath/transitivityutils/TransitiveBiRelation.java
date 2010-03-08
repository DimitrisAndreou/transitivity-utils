package edu.bath.transitivityutils;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public interface TransitiveBiRelation<E> extends TransitiveRelation<E> {
    TransitiveBiRelation<E> inverse();
}
