package edu.bath.transitivityutils;

import java.util.Collection;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public interface Navigator<E> {
    Collection<E> related(E subjectValue);
    Collection<E> domain();
}
