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
 * Class: MovePicker
 *
 * Description:
 * ------------
 * This class manages move generation, scoring, sorting, and selection for chess positions
 * during search. It supports heuristics like MVV-LVA (Most Valuable Victim - Least Valuable Attacker),
 * killer moves, PSQT bonuses, and basic tactical danger checks.
 *
 * Key functionalities include:
 * - Generating all legal moves for the current player.
 * - Scoring moves based on captures, promotions, killer move priority, and positional value.
 * - Sorting moves to maximize alpha-beta pruning effectiveness in the search.
 * - Tracking killer moves to improve move ordering at each search depth.
 *
 * Usage:
 * ------
 * - Instantiate with a ChessBoard, color, and search depth.
 * - Call `nextMove()` to iterate through moves in best-first order.
 * - Use `addKillerMove(move, depth)` to record strong quiet moves.
 *
 * Dependencies:
 * -------------
 * - ChessBoard (for move generation and evaluation)
 * - GameContext (to track turns and ply depth)
 * - Move (for move representation and utilities)
 * - Evaluator (for positional scoring using piece-square tables)
 *
 * Notes:
 * ------
 * Good move ordering is critical for efficient pruning in alpha-beta search.
 * This class focuses on prioritizing tactical and positional moves before quiet moves.
 * Future enhancements could include:
 * - History heuristic
 * - Late Move Reductions (LMR)
 * - Quiet move pruning
 */
package com.chess.montecarlo;

import com.chess.minimax.ChessBoard;
import com.chess.minimax.GameContext;
import java.util.*;

public class MovePicker {

    private final List<Move> moves;
    private int currentIndex = 0;
    private static final int MAX_DEPTH = 64;
    private static final Move[][] killerMoves = new Move[MAX_DEPTH][2]; // Two killer moves per depth

    /**
     * Constructs a MovePicker that generates and sorts all possible moves for a
     * given board state, player color, and search depth.
     *
     * @param board The current chess board state.
     * @param isWhite True if generating moves for the white player; false for
     * black.
     * @param depth The current search depth, used for move ordering heuristics.
     */
    public MovePicker(ChessBoard board, boolean isWhite, int depth) {
        this.moves = generateAndSortMoves(board, isWhite, depth);
    }

    /**
     * Retrieves the next best move from the sorted list of generated moves.
     * Returns null if there are no more moves to pick.
     *
     * @return The next Move object, or null if all moves have been exhausted.
     */
    public Move nextMove() {
        if (currentIndex < moves.size()) {
            return moves.get(currentIndex++);
        }
        return null;
    }

    /**
     * Generates all legal moves for the given player from the current board
     * state, scores them using heuristics (e.g., captures, promotions, killer
     * moves), and returns a list of moves sorted in descending order of their
     * estimated strength.
     *
     * @param board The current chess board state.
     * @param isWhite True if generating moves for the white player; false for
     * black.
     * @param depth The search depth used to prioritize certain types of moves
     * (e.g., killer moves).
     * @return A sorted list of Move objects, ordered by descending heuristic
     * score.
     */
    private List<Move> generateAndSortMoves(ChessBoard board, boolean isWhite, int depth) {
        GameContext.Player player = isWhite ? GameContext.Player.WHITE : GameContext.Player.BLACK;

        List<int[]> rawMoves = board.getAllLegalMoves(player);
        List<Move> allMoves = new ArrayList<>();
        for (int[] m : rawMoves) {
            int from = m[0] * 8 + m[1];
            int to = m[2] * 8 + m[3];

            int piece = board.getPieceAt(m[0], m[1]);
            boolean isPawn = Math.abs(piece) == 1;
            boolean isPromotionRank = (isWhite && m[2] == 0) || (!isWhite && m[2] == 7);

            if (isPawn && isPromotionRank) {
                allMoves.add(new Move(from, to, Move.QUEEN_PROMOTION));
            } else {
                allMoves.add(new Move(from, to));
            }
        }

        List<ScoredMove> scoredMoves = new ArrayList<>();
        for (Move move : allMoves) {
            int score = scoreMove(move, board, depth, isWhite);
            scoredMoves.add(new ScoredMove(move, score));
        }

        scoredMoves.sort(Comparator.comparingInt((ScoredMove sm) -> sm.score).reversed());

        List<Move> sorted = new ArrayList<>();
        for (ScoredMove sm : scoredMoves) {
            sorted.add(sm.move);
        }

        return sorted;
    }

    /**
     * Scores a move based on various heuristics including Most Valuable Victim
     * - Least Valuable Attacker (MVV-LVA), killer move heuristics, promotion
     * bonuses, positional factors, and potential dangers (such as being
     * captured).
     *
     * @param move The move to score.
     * @param board The current board state used for evaluation.
     * @param depth The current search depth (used for killer move detection).
     * @param isWhite True if the move is for the white player; false if for
     * black.
     * @return An integer representing the move's heuristic score (higher is
     * better).
     */
    private int scoreMove(Move move, ChessBoard board, int depth, boolean isWhite) {
        int[][] b = board.getBoard();
        int fromRow = move.getFrom() / 8;
        int fromCol = move.getFrom() % 8;
        int toRow = move.getTo() / 8;
        int toCol = move.getTo() % 8;

        int attacker = Math.abs(b[fromRow][fromCol]);
        int victim = Math.abs(b[toRow][toCol]);
        int score = 0;

        // MVV-LVA: Most Valuable Victim - Least Valuable Attacker
        if (victim != 0) {
            return 1000 + 10 * getPieceValue(victim) - getPieceValue(attacker);
        }

        // Killer heuristic: prioritize killer moves
        if (depth < MAX_DEPTH) {
            for (Move killer : killerMoves[depth]) {
                if (killer != null && Move.equals(killer, move)) {
                    return 900;
                }
            }
        }

        if (move.isPromotionMove()) {
            return 800;
        }

        if ((attacker == 3 || attacker == 4) && (fromRow == 7 || fromRow == 0)) {
            score += 100;
        }

        if (attacker == 1 && (fromRow == 6 || fromRow == 1) && (toCol == 3 || toCol == 4)) {
            score += 120;
        }

        if (attacker == 3 && GameContext.getPly() < 4 && (toCol == 0 || toCol == 7)) {
            score -= 100;
        }

        if (attacker == 2 && Math.abs(fromCol - toCol) + Math.abs(fromRow - toRow) == 1 && victim == 0) {
            score -= 50;
        }

        if ((attacker == 3 || attacker == 4) && ((fromRow == 5 || fromRow == 6) && (toRow == 7))
                || ((fromRow == 2 || fromRow == 1) && (toRow == 0))) {
            score -= 80;
        }

        if (wouldBeCaptured(move, board)) {
            score -= 100;
        }

        // PSQT bonus for quiet moves
        switch (attacker) {
            case 1 ->
                score += isWhite ? Evaluator.PAWN_MG[toRow][toCol] : -Evaluator.PAWN_MG[7 - toRow][toCol];
            case 2 ->
                score += isWhite ? Evaluator.ROOK_MG[toRow][toCol] : -Evaluator.ROOK_MG[7 - toRow][toCol];
            case 3 ->
                score += isWhite ? Evaluator.KNIGHT_MG[toRow][toCol] : -Evaluator.KNIGHT_MG[7 - toRow][toCol];
            case 4 ->
                score += isWhite ? Evaluator.BISHOP_MG[toRow][toCol] : -Evaluator.BISHOP_MG[7 - toRow][toCol];
            case 6 ->
                score += isWhite ? Evaluator.KING_MG[toRow][toCol] : -Evaluator.KING_MG[7 - toRow][toCol];
        }

        return score;
    }

    /**
     * Returns the material value of a given piece type. Positive integers
     * represent piece types (e.g., 1 = pawn, 5 = queen), and each type is
     * assigned a standard value.
     *
     * @param piece The piece type as an integer.
     * @return The material value associated with the piece type, or 0 if the
     * piece type is unrecognized.
     */
    private int getPieceValue(int piece) {
        return switch (piece) {
            case 1 ->
                100;
            case 2 ->
                500;
            case 3 ->
                320;
            case 4 ->
                330;
            case 5 ->
                900;
            case 6 ->
                20000;
            default ->
                0;
        };
    }

    /**
     * A helper class that associates a Move with its evaluated heuristic score.
     * Used for sorting moves based on their quality during move ordering.
     */
    private static class ScoredMove {

        /**
         * The move being evaluated.
         */
        Move move;

        /**
         * The heuristic score assigned to the move.
         */
        int score;

        /**
         * Constructs a ScoredMove object with a specified move and its
         * associated score.
         *
         * @param move The move being scored.
         * @param score The heuristic evaluation score of the move.
         */
        ScoredMove(Move move, int score) {
            this.move = move;
            this.score = score;
        }
    }

    /**
     * Determines if a move would result in the moving piece being captured on
     * the opponent's next turn. Simulates the move on a copy of the board,
     * generates all opponent responses, and checks if any can capture the moved
     * piece.
     *
     * @param move The move to test for capture vulnerability.
     * @param board The current board state to simulate the move on.
     * @return True if the moved piece could be captured immediately afterward;
     * false otherwise.
     */
    private boolean wouldBeCaptured(Move move, ChessBoard board) {
        ChessBoard copy = board.copy();
        int fromRow = move.getFrom() / 8;
        int fromCol = move.getFrom() % 8;
        int toRow = move.getTo() / 8;
        int toCol = move.getTo() % 8;

        copy.movePiece(fromRow, fromCol, toRow, toCol);

        GameContext.nextPlayer();
        List<int[]> enemyMoves = copy.getAllLegalMoves(GameContext.getCurrentPlayer());
        GameContext.nextPlayer();

        for (int[] m : enemyMoves) {
            if (m[2] == toRow && m[3] == toCol) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a move to the killer move table for a given depth, used to improve
     * move ordering in the search algorithm. If the move is not already the
     * primary killer move at that depth, it is inserted as the new primary, and
     * the previous primary is shifted to secondary.
     *
     * @param move The move to add as a killer move.
     * @param depth The search depth at which the move occurred.
     */
    public static void addKillerMove(Move move, int depth) {
        if (depth < MAX_DEPTH) {
            if (killerMoves[depth][0] == null || !Move.equals(killerMoves[depth][0], move)) {
                killerMoves[depth][1] = killerMoves[depth][0];
                killerMoves[depth][0] = move;
            }
        }
    }
}
