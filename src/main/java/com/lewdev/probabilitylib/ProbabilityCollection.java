/*
 * Copyright (c) 2020 Lewys Davies
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.lewdev.probabilitylib;

import java.util.*;
import java.util.function.IntUnaryOperator;

/**
 * ProbabilityCollection for retrieving random elements based on probability.
 * <br>
 * <br>
 * <b>Selection Algorithm Implementation</b>:
 * <p>
 * <ul>
 * <li>Elements have a "block" of space, sized based on their probability share
 * <li>"Blocks" start from index 1 and end at the total probability of all
 * elements
 * <li>A random number is selected between 1 and the total probability
 * <li>Which "block" the random number falls in is the element that is selected
 * <li>Therefore "block"s with larger probability have a greater chance of being
 * selected than those with smaller probability.
 * </p>
 * </ul>
 *
 * @param <E> Type of elements
 * @author Lewys Davies
 * @version 0.8
 */
public final class ProbabilityCollection<E> {
    private final List<ProbabilitySetElement<E>> collection = new LinkedList<>();
    private final IntUnaryOperator randomOperator;
    private int totalProbability = 0;

    /**
     * Create a new ProbabilityCollection with a custom random number generator
     *
     * @param randomNumberGenerator Random number generator that returns a random number between 0 and n-1
     */
    public ProbabilityCollection(IntUnaryOperator randomNumberGenerator) {
        this.randomOperator = randomNumberGenerator;
    }

    private ProbabilityCollection(SplittableRandom random) {
        this(random::nextInt);
    }

    /**
     * Create a new ProbabilityCollection with a default random number generator
     */
    public ProbabilityCollection() {
        this(new SplittableRandom());
    }

    /**
     * Create a new ProbabilityCollection with a default random number generator
     *
     * @param seed Seed for random number generator
     */
    public ProbabilityCollection(long seed) {
        this(new SplittableRandom(seed));
    }

    /**
     * Get the total of objects in this collection
     *
     * @return Number of objects inside the collection
     */
    public int size() {
        return this.collection.size();
    }

    /**
     * Check if collection is empty
     *
     * @return True if collection contains no elements, else False
     */
    public boolean isEmpty() {
        return this.collection.isEmpty();
    }

    /**
     * Check if collection contains an object
     *
     * @return True if collection contains the object, else False
     * @throws IllegalArgumentException if object is null
     */
    public boolean contains(E object) {
        if (object == null) {
            throw new IllegalArgumentException("Cannot check if null object is contained in this collection");
        }

        for (ProbabilitySetElement<E> entry : this.collection) {
            if (entry.getObject().equals(object)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the iterator for this collection
     *
     * @return Iterator over this collection
     */
    public Iterator<ProbabilitySetElement<E>> iterator() {
        return this.collection.iterator();
    }

    /**
     * Add an object to this collection
     *
     * @param object      object. Not null.
     * @param probability share. Must be greater than 0.
     * @throws IllegalArgumentException if object is null
     * @throws IllegalArgumentException if probability <= 0
     */
    public void add(E object, int probability) {
        if (object == null) {
            throw new IllegalArgumentException("Cannot add null object");
        }

        if (probability <= 0) {
            throw new IllegalArgumentException("Probability must be greater than 0");
        }

        ProbabilitySetElement<E> entry = new ProbabilitySetElement<>(object, probability);

        this.collection.add(entry);
        this.totalProbability += probability;
    }

    /**
     * Remove an object from this collection
     *
     * @param object object
     * @return True if object was removed, else False.
     * @throws IllegalArgumentException if object is null
     */
    public boolean remove(E object) {
        if (object == null) {
            throw new IllegalArgumentException("Cannot remove null object");
        }

        Iterator<ProbabilitySetElement<E>> it = this.iterator();
        boolean removed = false;

        // Remove all instances of the object
        while (it.hasNext()) {
            ProbabilitySetElement<E> entry = it.next();
            if (entry.getObject().equals(object)) {
                this.totalProbability -= entry.getProbability();
                it.remove();
                removed = true;
            }
        }

        return removed;
    }

    /**
     * Remove all objects from this collection
     */
    public void clear() {
        this.collection.clear();
        this.totalProbability = 0;
    }

    /**
     * Get a random object from this collection, based on probability.
     *
     * @return <E> Random object
     * @throws IllegalStateException if this collection is empty
     */
    public E get() {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot get an object out of a empty collection");
        }

        int random = this.randomOperator.applyAsInt(this.totalProbability);

        int index = 0;
        for (ProbabilitySetElement<E> entry : this.collection) {
            index += entry.getProbability();
            if (random < index) {
                return entry.getObject();
            }
        }

        throw new IllegalStateException("Failed to get an object from the collection");
    }

    /**
     * Get the total probability of all elements in this collection
     *
     * @return Sum of all element's probability
     */
    public int getTotalProbability() {
        return this.totalProbability;
    }

    /**
     * Used internally to store information about an object's state in a collection.
     * Specifically, the probability and index within the collection.
     * <p>
     * Indexes refer to the start position of this element's "block" of space. The
     * space between element "block"s represents their probability of being selected
     *
     * @param <T> Type of element
     * @author Lewys Davies
     */
    public static final class ProbabilitySetElement<T> {
        private final T object;
        private final int probability;

        /**
         * Create a new pair of object and probability
         *
         * @param object      object
         * @param probability share within the collection
         */
        private ProbabilitySetElement(T object, int probability) {
            this.object = object;
            this.probability = probability;
        }

        /**
         * Get the object
         *
         * @return <T> The actual object
         */
        public T getObject() {
            return this.object;
        }

        /**
         * Get the probability share of this object
         *
         * @return Probability share in this collection
         */
        public int getProbability() {
            return this.probability;
        }
    }
}
