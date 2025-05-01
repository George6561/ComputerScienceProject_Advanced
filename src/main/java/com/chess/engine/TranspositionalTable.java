/*
 * Copyright (c) 2024
 * George Miller
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * ----------------------------------------------------------------------------
 *
 * Class: TranspositionalTable
 *
 * Description:
 * ------------
 * This class implements a basic transposition table (hash table) for storing and retrieving
 * previously evaluated chess positions. It is used to speed up the search process by avoiding
 * redundant evaluations of the same board state.
 *
 * Key functionalities include:
 * - Caching evaluated positions based on Zobrist keys.
 * - Storing depth, evaluation score, and bound type (exact, lower, or upper).
 * - Replacing entries only if the new search depth is greater than or equal to the existing entry.
 * - Quick lookup to determine if a board state has already been evaluated at sufficient depth.
 *
 * Usage:
 * ------
 * - Call `store(key, depth, score, bound)` after evaluating a position.
 * - Use `retrieve(key)` to access stored evaluation data.
 * - Call `contains(key, depth)` to check if a usable entry exists.
 * - Clear the table between games or searches using `clear()`.
 *
 * Dependencies:
 * -------------
 * - None external. Relies on Java's standard HashMap.
 *
 * Notes:
 * ------
 * This is a simple but effective transposition table.
 * Future improvements could include:
 * - Implementing an aging mechanism (e.g., replacement schemes like Always Replace, Depth Preferred, etc.).
 * - Limiting table size to prevent memory overflow in long-running sessions.
 * - Supporting multiple probes per key (to store multiple entries if needed).
 */
package com.chess.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * A transpositional table used to cache previously evaluated board positions
 * during the search to avoid redundant computations and improve efficiency.
 * Each position is stored with its evaluation score, search depth, and bound type.
 */
public class TranspositionalTable {

    /**
     * Enumeration to represent the type of score bound stored in the transpositional table.
     */
    public enum BoundType {
        /** Exact score evaluation. */
        EXACT,
        /** Lower bound on the score (alpha). */
        LOWER_BOUND,
        /** Upper bound on the score (beta). */
        UPPER_BOUND
    }

    /**
     * Represents an entry in the transpositional table containing a depth, score, and bound type.
     */
    public static class TTEntry {
        /** The depth at which the position was evaluated. */
        public final int depth;
        /** The evaluation score of the position. */
        public final int score;
        /** The type of bound associated with the score. */
        public final BoundType bound;

        /**
         * Constructs a transpositional table entry with a given depth, score, and bound type.
         *
         * @param depth The search depth of the evaluation.
         * @param score The evaluation score.
         * @param bound The bound type (EXACT, LOWER_BOUND, or UPPER_BOUND).
         */
        public TTEntry(int depth, int score, BoundType bound) {
            this.depth = depth;
            this.score = score;
            this.bound = bound;
        }
    }

    /** The underlying storage mapping Zobrist hash keys to their evaluation entries. */
    private final Map<Long, TTEntry> table;

    /**
     * Constructs an empty transpositional table.
     */
    public TranspositionalTable() {
        this.table = new HashMap<>();
    }

    /**
     * Stores a new evaluation entry into the table.
     * Only overwrites an existing entry if the new depth is greater or equal to the stored one.
     *
     * @param zobristKey The unique hash representing the board position.
     * @param depth The search depth associated with the evaluation.
     * @param score The evaluation score.
     * @param bound The type of bound for the stored score.
     */
    public void store(long zobristKey, int depth, int score, BoundType bound) {
        TTEntry existing = table.get(zobristKey);

        // Only store if deeper or no existing entry
        if (existing == null || depth >= existing.depth) {
            table.put(zobristKey, new TTEntry(depth, score, bound));
        }
    }

    /**
     * Retrieves the evaluation entry for a given board position.
     *
     * @param zobristKey The unique hash representing the board position.
     * @return The stored TTEntry, or null if not present.
     */
    public TTEntry retrieve(long zobristKey) {
        return table.get(zobristKey);
    }

    /**
     * Checks if a sufficiently deep evaluation exists for a given board position.
     *
     * @param zobristKey The unique hash representing the board position.
     * @param depth The minimum required depth.
     * @return True if an entry exists at least at the given depth; false otherwise.
     */
    public boolean contains(long zobristKey, int depth) {
        TTEntry entry = table.get(zobristKey);
        return entry != null && entry.depth >= depth;
    }

    /**
     * Clears all entries from the transpositional table.
     */
    public void clear() {
        table.clear();
    }
}
