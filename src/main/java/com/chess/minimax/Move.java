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
 * Class: Move
 *
 * Description:
 * ------------
 * This class represents a basic chess move, characterized by a starting square (from)
 * and a destination square (to). Moves are encoded using integer values based on
 * the flattened 1D representation of an 8x8 chessboard.
 *
 * Key functionalities include:
 * - Storing the source and destination squares of a move.
 * - Providing accessor methods for the move's details.
 * - Indicating whether the move is a pawn promotion (currently always false; can be enhanced).
 *
 * Usage:
 * ------
 * - Used by MinimaxEngine, ChessBoard, and other components to represent potential moves.
 * - Can be extended later to support advanced move types such as promotions, castling, or en passant.
 *
 * Dependencies:
 * -------------
 * - None. Lightweight data holder class.
 *
 * Notes:
 * ------
 * This class is intentionally simple for fast evaluation in the Minimax search tree.
 * For more complex move types, consider extending the Move class to include move flags.
 */
package com.chess.minimax;

public class Move {

    /** The starting square of the move, represented as a single integer index. */
    private final int from;

    /** The destination square of the move, represented as a single integer index. */
    private final int to;

    /**
     * Constructs a Move object with the specified origin and destination squares.
     *
     * @param from The starting square index (0–63).
     * @param to The destination square index (0–63).
     */
    public Move(int from, int to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Gets the starting square of the move.
     *
     * @return The origin square index.
     */
    public int getFrom() {
        return from;
    }

    /**
     * Gets the destination square of the move.
     *
     * @return The target square index.
     */
    public int getTo() {
        return to;
    }

    /**
     * Checks if the move is a promotion move (e.g., pawn promotion).
     * Currently always returns false; can be extended for full promotion support.
     *
     * @return False (placeholder implementation).
     */
    public boolean isPromotionMove() {
        return false; // You can improve this later
    }
}
