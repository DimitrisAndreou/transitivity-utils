package edu.bath.transitivityutils;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Random;

final class Distributions {
    private Distributions() { }

    /**
     *
     * A random sample of natural, where each choice is associated with a probability. This implementation
     * is based on the fast <em>alias</em> method, where for each random choice two random
     * numbers are generated and only a single table lookup performed.
     *
     * @param <T> the type of the choices to be made
     * @see <a href="http://cg.scs.carleton.ca/~luc/rnbookindex.html">L. Devroye, Non-Uniform Random Variate Generation, 1986, p. 107</a>
     * @param random
     * @param restProbs
     * @return
     */
    public static Iterator<Integer> random(final Random random, double firstProb, double... restProbs) {
        double sum = checkValidProbability(firstProb);
        for (double prob : restProbs) {
            sum += checkValidProbability(prob);
        }

        final double[] normalizedProbs = new double[1 + restProbs.length];
        for (int i = 0; i < restProbs.length; i++) {
            normalizedProbs[i] = restProbs[i] * restProbs.length / sum; //average(normalizedProbs) = 1.0
        }

        int expectedSize = restProbs.length / 2 + 2;
        Deque<Integer> smaller = new ArrayDeque<Integer>(expectedSize);
        Deque<Integer> greater = new ArrayDeque<Integer>(expectedSize);
        for (int i = 0; i < normalizedProbs.length; i++) {
            if (normalizedProbs[i] < 1.0) {
                smaller.push(i);
            } else {
                greater.push(i);
            }
        }

        final int[] indexes = new int[restProbs.length];
        while (!smaller.isEmpty()) {
            Integer i = smaller.pop();
            Integer k = greater.peek();
            indexes[i] =  k;
            normalizedProbs[k] -= (1.0 - normalizedProbs[i]);
            if (normalizedProbs[k] < 1.0) {
                greater.pop();
                smaller.push(k);
            }
        }

        return new AbstractIterator<Integer>() {
            @Override
            protected Integer computeNext() {
                int index = random.nextInt(normalizedProbs.length);
                double x = random.nextDouble();
                return x < normalizedProbs[index] ? index : indexes[index];
            }
        };
    }

    private static double checkValidProbability(double x) {
        Preconditions.checkArgument(x >= 0.0, "Negative probability");
        return x;
    }

    public static void main(String[] args) {
        Multiset<Integer> bag = TreeMultiset.create();
        Iterator<Integer> sample = random(new Random(), 0.2, 0.2, 0.1, 0.4, 0.1);
        for (int i = 0; i < 200000; i++) {
            bag.add(sample.next());
        }
        System.out.println(bag);
    }
}
