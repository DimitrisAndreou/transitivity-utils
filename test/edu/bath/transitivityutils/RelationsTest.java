package edu.bath.transitivityutils;

import com.google.common.collect.ImmutableSetMultimap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static edu.bath.transitivityutils.RelationAssertions.*;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public class RelationsTest {

    public RelationsTest() {
    }

    @Test
    public void testMerge() {
        TransitiveRelation<Integer> rel = Relations.newTransitiveRelation();

        Relations.merge(rel, Navigators.forMultimap(ImmutableSetMultimap.of(
                1, 2,
                2, 3,
                3, 4)));

        assertRelations(rel,
                1, 2,
                1, 3,
                1, 4,
                2, 3,
                2, 4,
                3, 4);
    }
}