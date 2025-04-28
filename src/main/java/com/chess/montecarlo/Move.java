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
 * This class represents a chess move in an efficient encoded format, optimized
 * for fast search operations within the Chess Master engine. Moves are internally
 * stored as 16-bit integers to reduce memory footprint and improve performance.
 *
 * Key functionalities include:
 * - Encoding and decoding moves between (from, to, promotion) fields and 16-bit representation.
 * - Supporting special move types such as pawn promotions.
 * - Providing utility methods for move comparison, capture detection, and undoing moves.
 * - String representation of moves in standard algebraic notation (e.g., e2e4, e7e8Q).
 *
 * Usage:
 * ------
 * - Create moves via constructor or `encodeMove()` method.
 * - Decode moves using `decodeMove()` when needed.
 * - Use `isCapture()` and `isPromotionMove()` to check move types.
 *
 * Dependencies:
 * -------------
 * - ChessBoard (to determine capture status)
 *
 * Notes:
 * ------
 * This class supports pawn promotions but does not explicitly encode castling
 * or en passant flags separately. These features are typically handled at the board level.
 * Future expansions could include full move flags for complete move semantics.
 */
package com.chess.montecarlo;

import com.chess.minimax.ChessBoard;

public class Move {

    // Encoding squares as 16-bit values (e.g., (row * 8 + col) for a square)
    private static final int FROM_MASK = 0x3F;
    private static final int TO_MASK = 0xFC0;
    private static final int PROMOTION_MASK = 0xF000;

    // Square values are 0 to 63, representing the 8x8 chessboard
    private final int move;
    private final int from;
    private final int to;
    private final int promotion; // Only applies to pawn promotion
    private final boolean isPromotionMove;

    // Static piece types for promotions
    public static final int NO_PROMOTION = 0;
    public static final int QUEEN_PROMOTION = 1;
    public static final int ROOK_PROMOTION = 2;
    public static final int KNIGHT_PROMOTION = 3;
    public static final int BISHOP_PROMOTION = 4;

    /**
     * Constructs a Move object with the specified origin and destination
     * squares, assuming no promotion is involved.
     *
     * @param from The starting square index (0–63).
     * @param to The destination square index (0–63).
     */
    public Move(int from, int to) {
        this(from, to, NO_PROMOTION);
    }

    /**
     * Constructs a Move object with the specified origin, destination, and
     * promotion information. Encodes the move into a compact integer format for
     * efficient storage and processing.
     *
     * @param from The starting square index (0–63).
     * @param to The destination square index (0–63).
     * @param promotion The promotion piece type, or NO_PROMOTION if no
     * promotion occurs.
     */
    public Move(int from, int to, int promotion) {
        this.move = (from & FROM_MASK) | ((to & FROM_MASK) << 6) | (promotion << 12);
        this.from = from;
        this.to = to;
        this.promotion = promotion;
        this.isPromotionMove = (promotion != NO_PROMOTION);
    }

    /**
     * Returns the starting square index of the move.
     *
     * @return The origin square (0–63).
     */
    public int getFrom() {
        return from;
    }

    /**
     * Returns the destination square index of the move.
     *
     * @return The target square (0–63).
     */
    public int getTo() {
        return to;
    }

    /**
     * Returns the promotion piece type for this move.
     *
     * @return The promotion type, or NO_PROMOTION if the move does not involve
     * promotion.
     */
    public int getPromotion() {
        return promotion;
    }

    /**
     * Checks if the move is a pawn promotion move.
     *
     * @return True if the move involves promotion; false otherwise.
     */
    public boolean isPromotionMove() {
        return isPromotionMove;
    }

    /**
     * Encodes a move by creating a new Move object with the specified origin,
     * destination, and promotion type.
     *
     * @param from The starting square index (0–63).
     * @param to The destination square index (0–63).
     * @param promotion The promotion piece type, or NO_PROMOTION if no
     * promotion occurs.
     * @return A Move object representing the encoded move.
     */
    public static Move encodeMove(int from, int to, int promotion) {
        return new Move(from, to, promotion);
    }

    /**
     * Decodes a compact integer move code into a Move object by extracting the
     * origin, destination, and promotion fields.
     *
     * @param moveCode The encoded move as an integer.
     * @return A Move object representing the decoded move.
     */
    public static Move decodeMove(int moveCode) {
        int from = moveCode & FROM_MASK;
        int to = (moveCode >> 6) & FROM_MASK;
        int promotion = (moveCode >> 12) & 0xF;
        return new Move(from, to, promotion);
    }

    @Override
    public String toString() {
        char fromCol = (char) ('a' + (from % 8));
        char fromRow = (char) ('1' + (from / 8));
        char toCol = (char) ('a' + (to % 8));
        char toRow = (char) ('1' + (to / 8));
        String moveStr = "" + fromCol + fromRow + toCol + toRow;

        // Add promotion if necessary (e.g. e7e8Q)
        if (isPromotionMove) {
            char promoChar = getPromotionChar(promotion);
            moveStr += promoChar;
        }
        return moveStr;
    }

    /**
     * Converts a promotion piece type into its corresponding character
     * representation. Used for displaying promoted pieces in move notation
     * (e.g., 'Q' for Queen, 'N' for Knight).
     *
     * @param promotion The promotion type constant.
     * @return A character representing the promotion piece ('Q', 'R', 'N', or
     * 'B').
     * @throws IllegalArgumentException If the promotion type is invalid.
     */
    private char getPromotionChar(int promotion) {
        switch (promotion) {
            case QUEEN_PROMOTION -> {
                return 'Q';
            }
            case ROOK_PROMOTION -> {
                return 'R';
            }
            case KNIGHT_PROMOTION -> {
                return 'N';
            }
            case BISHOP_PROMOTION -> {
                return 'B';
            }
            default ->
                throw new IllegalArgumentException("Invalid promotion type");
        }
    }

    /**
     * Checks whether two moves are equal by comparing their starting and
     * destination squares.
     *
     * @param move1 The first move to compare.
     * @param move2 The second move to compare.
     * @return True if both moves have the same origin and destination squares;
     * false otherwise.
     */
    public static boolean equals(Move move1, Move move2) {
        return move1.getFrom() == move2.getFrom() && move1.getTo() == move2.getTo();
    }

    /**
     * Returns the starting and destination squares of a move as an array.
     *
     * @param move The move from which to extract the squares.
     * @return An integer array where the first element is the origin square and
     * the second is the destination square.
     */
    public static int[] getSquares(Move move) {
        return new int[]{move.getFrom(), move.getTo()};
    }

    /**
     * Determines whether this move results in the capture of an opponent's
     * piece.
     *
     * @param board The chess board to check against.
     * @return True if the destination square is occupied by a piece; false
     * otherwise.
     */
    public boolean isCapture(ChessBoard board) {
        return board.getBoard()[to / 8][to % 8] != 0; // Any non-zero value means capture
    }

    /**
     * Creates a new Move object that reverses this move, swapping the origin
     * and destination squares.
     *
     * @return A new Move representing the undo of the current move.
     */
    public Move undoMove() {
        return new Move(to, from, promotion); // reverse the from and to
    }

    /**
     * Creates and returns a special "no move" object, typically used to
     * represent an invalid or placeholder move.
     *
     * @return A Move object with invalid square indices (-1, -1) and no
     * promotion.
     */
    public static Move noMove() {
        return new Move(-1, -1, NO_PROMOTION); // Invalid move
    }

}
