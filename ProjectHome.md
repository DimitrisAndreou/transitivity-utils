# Transitivity Utilities #

## Introduction ##

The main purpose of this project is to make maintenance of transitive relations fast, and querying whether two objects are related extremely fast, in Java.

For example, consider a usual class hierarchy: "extends" is a transitive relation, i.e. if class A **extends** class B, and B **extends** C, then A also **extends** C. Given such a class hierarchy, one could answer "does _A_ **extend** _C_?" by materializing the transitive closure of _A_, which is both slow and expensive in terms of memory. Even a tree, having O(N) direct relationships, may induce O(N^2) transitive relationships, thus a naive representation would require that much space.

This project provides O(1) query time for trees, using just O(N) space, and O(logn) query time for general graphs with nominal worst case memory usage of O(N^2) (if the relation is a full bipartite graph), which can be much less in practice.

This is the core type provided here:

```
interface Relation<E> {
  void relate(E subj, E obj);
  boolean areRelated(E subj, E obj);
}
```
(Extended by `TransitiveRelation`, which guarantees transitivity of the above relation).

In this blog post: [Graph reachability, transitive closures, and a nasty historical accident in the pre-google era](http://code-o-matic.blogspot.com/2010/07/graph-reachability-transitive-closures.html), I explain the key idea that made this project viable.

See [Documentation](Documentation.md), [Javadocs](http://transitivity-utils.googlecode.com/svn/trunk/dist/javadoc/index.html), or [Design](Design.md) for further information.

Nightly builds available through hudson:
https://alis.cs.bath.ac.uk/hudson/job/transitivity-utils/