package edu.bath.transitivityutils;

import java.util.Set;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public interface Navigator<E> {
    Set<E> related(E subject);
    Set<E> domain();
}
