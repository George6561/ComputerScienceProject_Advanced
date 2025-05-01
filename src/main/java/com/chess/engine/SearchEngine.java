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
 * Class: SearchEngine
 *
 * Description:
 * ------------
 * This class implements an advanced chess search algorithm using alpha-beta pruning,
 * quiescence search, and transposition tables to efficiently explore and evaluate
 * possible move sequences.
 *
 * Key functionalities include:
 * - Alpha-beta pruning to eliminate unnecessary branches during search.
 * - Quiescence search to extend evaluation past unstable (capture-heavy) positions.
 * - Transpositional table caching to avoid redundant board evaluations.
 * - Integration with an Evaluator for scoring board positions.
 * - Move ordering using MovePicker with killer heuristics and basic MVV-LVA principles.
 *
 * Usage:
 * ------
 * - Instantiate SearchEngine and call `findBestMove(board, depth)` to determine the optimal move.
 * - Intended for mid-to-high level AI play with reasonable performance and strength.
 *
 * Dependencies:
 * -------------
 * - ChessBoard (managing board state)
 * - GameContext (tracking players and moves)
 * - Move (representing moves)
 * - MovePicker (move ordering and generation)
 * - Evaluator (position evaluation)
 * - TranspositionalTable (caching explored positions)
 *
 * Notes:
 * ------
 * This engine supports:
 * - Exact score caching via Zobrist hashing (hash function approximated here).
 * - Normalization of scores to always maximize from White's perspective.
 * - Optional future improvements:
 *   - Implement Aspiration Windows for faster pruning.
 *   - Integrate Late Move Reductions (LMR) for deeper search acceleration.
 *   - Add Principal Variation Search (PVS) for enhanced top-move handling.
 */
package com.chess.engine;

import com.chess.minimax.ChessBoard;
import com.chess.minimax.GameContext;
import java.util.List;

public class SearchEngine {

    private final Evaluator evaluator;
    private final TranspositionalTable tt;

    /**
     * Constructs a new SearchEngine instance, initializing its internal
     * evaluator and transpositional table for position scoring and search
     * optimization.
     */
    public SearchEngine() {
        this.evaluator = new Evaluator();
        this.tt = new TranspositionalTable();
    }

    /**
     * Finds the best move for the current player using alpha-beta pruning.
     *
     * @param board The current position
     * @param depth The search depth
     * @return The best move found
     */
    public Move findBestMove(ChessBoard board, int depth) {
        boolean isWhite = GameContext.isWhiteToMove();
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE; // Always maximize after normalization

        MovePicker movePicker = new MovePicker(board, isWhite, depth);
        Move move;

        while ((move = movePicker.nextMove()) != null) {
            ChessBoard copy = board.copy();
            applyMove(copy, move);

            GameContext.nextPlayer();
            int score = alphaBeta(copy, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, !isWhite);
            GameContext.nextPlayer();

            if (!isWhite) {
                score = -score;
            }

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    /**
     * Performs an alpha-beta pruned minimax search to evaluate the best
     * achievable score from the given board state. Uses a transpositional table
     * for caching positions and improving search efficiency.
     *
     * @param board The current chess board to evaluate.
     * @param depth The remaining search depth.
     * @param alpha The best already explored option along the path to the root
     * for the maximizer.
     * @param beta The best already explored option along the path to the root
     * for the minimizer.
     * @param isWhite True if the current player is white; false if black.
     * @return The evaluation score of the best move sequence from the current
     * position.
     */
    private int alphaBeta(ChessBoard board, int depth, int alpha, int beta, boolean isWhite) {
        long key = board.hash();

        if (tt.contains(key, depth)) {
            return tt.retrieve(key).score;
        }

        if (depth == 0) {
            int eval = quiescence(board, alpha, beta, isWhite, depth);
            tt.store(key, depth, eval, TranspositionalTable.BoundType.EXACT);
            return eval;
        }

        if (board.isCheckmate(GameContext.getCurrentPlayer())) {
            return -100000 + (5 - depth); // negative score for losing, prefer delaying checkmate
        }

        MovePicker movePicker = new MovePicker(board, isWhite, depth);
        Move move;
        int bestScore = Integer.MIN_VALUE;

        while ((move = movePicker.nextMove()) != null) {
            ChessBoard copy = board.copy();
            applyMove(copy, move);

            GameContext.nextPlayer();
            int eval = alphaBeta(copy, depth - 1, alpha, beta, !isWhite);
            GameContext.nextPlayer();

            if (!isWhite) {
                eval = -eval;
            }

            bestScore = Math.max(bestScore, eval);
            alpha = Math.max(alpha, eval);

            if (beta <= alpha) {
                break; // beta cutoff
            }
        }

        tt.store(key, depth, bestScore, TranspositionalTable.BoundType.EXACT);
        return bestScore;
    }

    /**
     * Applies a move to the given chess board, handling normal moves and
     * promotions. If the move is a promotion, replaces the pawn with the
     * promoted piece according to the promotion type.
     *
     * @param board The chess board to apply the move on.
     * @param move The move to apply, including promotion information if
     * applicable.
     */
    private void applyMove(ChessBoard board, Move move) {
        int fromRow = move.getFrom() / 8;
        int fromCol = move.getFrom() % 8;
        int toRow = move.getTo() / 8;
        int toCol = move.getTo() % 8;

        board.movePiece(fromRow, fromCol, toRow, toCol);

        if (move.isPromotionMove()) {
            boolean isWhite = GameContext.isWhiteToMove();
            int promotionPiece = switch (move.getPromotion()) {
                case Move.ROOK_PROMOTION ->
                    isWhite ? 2 : -2;
                case Move.KNIGHT_PROMOTION ->
                    isWhite ? 3 : -3;
                case Move.BISHOP_PROMOTION ->
                    isWhite ? 4 : -4;
                case Move.QUEEN_PROMOTION ->
                    isWhite ? 5 : -5;
                default ->
                    0;
            };
            board.addPiece(toRow, toCol, promotionPiece);
        }
    }

    /**
     * Performs a quiescence search to extend the evaluation at leaf nodes where
     * tactical moves (captures) are available. Helps avoid the "horizon effect"
     * by continuing the search only through capture moves until the position is
     * quiet.
     *
     * @param board The current chess board state.
     * @param alpha The current alpha value (lower bound for maximizer).
     * @param beta The current beta value (upper bound for minimizer).
     * @param isWhite True if the current player is white; false if black.
     * @param depth The current search depth used for move sorting purposes.
     * @return The best evaluation score found during the quiescence search.
     */
    private int quiescence(ChessBoard board, int alpha, int beta, boolean isWhite, int depth) {
        int standPat = evaluator.evaluate(board);

        if (standPat >= beta) {
            return beta;
        }

        if (alpha < standPat) {
            alpha = standPat;
        }

        MovePicker movePicker = new MovePicker(board, isWhite, depth);
        Move move;

        while ((move = movePicker.nextMove()) != null) {
            if (!move.isCapture(board)) {
                continue; // Only consider captures
            }

            ChessBoard copy = board.copy();
            applyMove(copy, move);

            GameContext.nextPlayer();
            int score = -quiescence(copy, -beta, -alpha, !isWhite, depth);
            GameContext.nextPlayer();

            if (score >= beta) {
                return beta;
            }

            if (score > alpha) {
                alpha = score;
            }
        }

        return alpha;
    }

}
