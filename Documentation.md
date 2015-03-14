The most important types offered here are that of
[TransitiveRelation](http://transitivity-utils.googlecode.com/svn/trunk/dist/javadoc/index.html?edu/bath/transitivityutils/TransitiveRelation.html), which is a specialization of [Relation](http://transitivity-utils.googlecode.com/svn/trunk/dist/javadoc/index.html?edu/bath/transitivityutils/Relation.html).

A `TransitiveRelation` is a relation that includes transitivity, i.e. if A => B (where this notation means that "A is related to B") and B => C, then A => C too.

Where are transitive relations used? Imagine a collection of Java class/interfaces - subtyping is a transitive relation. Or classes, with a subsumption relation, in a knowledge base. Or a directed graph, where you want to ask whether a node "is reachable" (via a directed path) from another - reachability is a transitive relation too.

A `TransitiveRelation` supports these methods:
```
TransitiveRelation<E> {
    void relate(E subj, E obj);
    boolean areRelated(E subj, E obj);
}
```
(Currently, an `unrelate(E, E)` is not offered).

Also, a `TransitiveRelation` includes this method:
```
TransitiveRelation<E> {
    Navigator<E> direct();
}
```
This provides access to the relationships that were explicitly added to the `TransitiveRelation` (via the `relate` method), not induced merely by transitivity.

This abstraction can be used to encode any aforementioned transitive relation. For example, if we iterate over a set of java types, and `relate` each one with each (immediate) supertype, we immediately have an efficient implementation of the `instanceof` operator in Java: `subtype.areRelated(subType, superType)`.

### TransitiveRelation ###

To create a `TransitiveRelation`, say on strings, use this:
```
   TransitiveRelation<String> relation = Relations.newTransitiveRelation();
```

We can test the created relation as follows:
```
   relation.relate("A", "B"); // A => B
   relation.relate("B", "C"); // B => C

   boolean result = relation.areRelated("A", "C"); // result == true
```

Note that the relation is not necessarily symmetric, i.e. A => B does not imply B => A, unless you either add the inverse relationship or it is implied by transitivity.

### Navigator ###

We already mentioned that a `TransitiveRelation` also remembers the relationships explicitly added to it. Lets explore this concept.

```
  //following the above example
  Navigator<String> direct = relation.direct();

  Set<String> domain = direct.domain(); // {"A", "B"}
  Set<String> relatedToA = direct.related("A"); // {"B"}
  Set<String> relatedToB = direct.related("B"); // ("C");
```

Given a `Navigator` instance, we can also compute transitive closures:
```
  Navigator<String> direct = relation.direct();

  Set<String> closureOfA = Navigators.closure(direct, "A"); // {"A", "B", "C"}
  Set<String> closureOfB = Navigators.closure(direct, "A"); // {"B", "C"}
  Set<String> closureOfC = Navigators.closure(direct, "A"); // {"C"}
```

### TransitiveBiRelation ###

The `Navigator` instance provided by the `direct()` method of `TransitiveRelation` allows one to ask "with which elements is A related _to_", i.e. a query of "A=>?". But this cannot answer the inverse query, "which elements are related _to_ A", i.e. the query of "?=>A". That's where a `TransitiveBiRelation` is useful - it keeps track of both directions. `TransitiveBiRelation` is itself a normal `TransitiveBiRelation`, but also includes the inverse method:
```
interface TransitiveBiRelation<E> extends TransitiveRelation<E> {
    TransitiveBiRelation<E> inverse();
}
```

This can be created as follows:
```
  TransitiveBiRelation<String> birelation = Relations.newTransitiveBiRelation();
```

Now, whereas `birelation.direct()` yields a navigator of the straight direction, `birelation.inverse().direct()` yields a navigator with the opposite direction, which can be used for example to find the transitive closure on the inverse relation (i.e. "all elements from where a transitive relationship exists _to_ some element A").

## Performance Considerations ##

While the implemented `TransitiveRelation` does support incremental updates (adding relationships), if you already have a whole bunch of relationships to insert, not all orders of insertion are created equal, both in terms of required memory and query performance. The basic concept is that "first a spanning tree of relationships should be added, then all the relationships remaining", because such a tree only requires O(1) space per element, and may capture, in such a compact way, a large part of the transitive relation. But to complicate things even more, not all spanning trees are created equal.

Two heuristics are offered out of the box. One works for any relation, the other only in acyclic ones (but produces very compact representations).

First, we must have a `Navigator<E>` object representing the relationships we wish to add to a `TransitiveRelation<E>`. Then, we just call one of these methods:
```
  Navigator<E> newRelationships = ...;
  TransitiveRelation<E> relation = ...;
  
  Relations.merge(relation, newRelationships); //for the general case

  //or

  Relations.mergeAcyclic(relation, newRelationships); //if we know the new relationships are acyclic - an exception will be thrown if this is not the case
```

An easy way to create a `Navigator<E>` is through a [Multimap](http://google-collections.googlecode.com/svn/trunk/javadoc/index.html?com/google/common/collect/Multimap.html) (of [google's collections](http://code.google.com/p/google-collections/)/[guava](http://code.google.com/p/guava-libraries/)). For example:

```
  Multimap<String, String> mm = HashMultimap.create();
  mm.put("A", "B"); //A => B
  mm.put("B", "D"); //etc.
  mm.put("A", "C");
  mm.put("C", "D");
  mm.put("C", "E");
  //ImmutableMultimap can also be used

  Navigator<String> nav = Navigators.forMultimap(mm);
```

Then, we just merge this navigator to the `TransitiveRelation`, and all its relationships are encoded to the latter.

## Javadocs ##

http://transitivity-utils.googlecode.com/svn/trunk/dist/javadoc/index.html