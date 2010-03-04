package edu.bath.transitivityutils;

import java.util.Iterator;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public interface OrderList<E> {
    Node<E> base();
    boolean delete(Node<E> node);

    Iterator<E> iterator();

    //if node does not originate from this list instance, undefined behavior
    Node<E> addAfter(Node<E> node, E value);

    interface Node<E> {
        boolean precedes(Node<?> n);
        boolean isValid();

        Node<E> next();
        Node<E> previous();

        E get();
        E set(E newValue);
    }
}
