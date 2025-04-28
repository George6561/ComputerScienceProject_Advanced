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
 * Class: MinimaxEngine
 *
 * Description:
 * ------------
 * This class implements a basic Minimax search algorithm for selecting the best move
 * in the Chess Master application. It evaluates all possible future moves up to a given depth,
 * assuming optimal play from both players.
 *
 * Key functionalities include:
 * - Searching the move tree using the Minimax algorithm.
 * - Selecting the best move for the current player (White or Black).
 * - Evaluating board states based on simple material balance (piece values).
 * - Handling ties between moves by choosing randomly among equally good options.
 *
 * Usage:
 * ------
 * - Call `findBestMove(board, depth)` to get the best move at a given search depth.
 * - Used primarily in Computer vs Computer or Human vs Computer modes with simpler AI.
 *
 * Dependencies:
 * -------------
 * - ChessBoard (for board state management and move generation)
 * - GameContext (to determine the current player)
 * - Move (to represent moves)
 *
 * Notes:
 * ------
 * This implementation does not include advanced techniques such as alpha-beta pruning,
 * move ordering, or quiescence search. For stronger performance, consider upgrading
 * to SearchEngine or using StockfishConnector integration.
 */
package com.chess.minimax;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MinimaxEngine {

    /**
     * Finds the best move for the current player using the minimax algorithm up
     * to a specified search depth. Evaluates all legal moves and selects the
     * move leading to the best evaluated board state. If multiple moves have
     * the same best evaluation, one is selected randomly.
     *
     * @param board The current chess board state.
     * @param depth The maximum search depth for the minimax algorithm.
     * @return The best move found, or null if no legal moves are available.
     */
    public Move findBestMove(ChessBoard board, int depth) {
        GameContext.Player currentPlayer = board.currentPlayer();
        boolean isMaximizing = currentPlayer == GameContext.Player.WHITE;

        int bestScore = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        List<Move> bestMoves = new ArrayList<>();
        List<int[]> legalMoves = board.getAllLegalMoves(currentPlayer);

        for (int[] move : legalMoves) {
            ChessBoard newBoard = board.copy();
            newBoard.movePiece(move[0], move[1], move[2], move[3]);

            int score = minimax(newBoard, depth - 1, !isMaximizing);

            Move currentMove = new Move(move[0] * 8 + move[1], move[2] * 8 + move[3]);

            if (score == bestScore) {
                bestMoves.add(currentMove);
            } else if ((isMaximizing && score > bestScore) || (!isMaximizing && score < bestScore)) {
                bestMoves.clear();
                bestMoves.add(currentMove);
                bestScore = score;
            }
        }

        if (!bestMoves.isEmpty()) {
            return bestMoves.get(new Random().nextInt(bestMoves.size()));
        }

        return null;
    }

    /**
     * Recursively applies the minimax algorithm to evaluate the best achievable
     * score from the current board state, assuming optimal play from both
     * sides. Used to determine the strength of a position up to a given search
     * depth.
     *
     * @param board The current chess board state to evaluate.
     * @param depth The remaining depth to search.
     * @param isMaximizing True if the current player is maximizing their score;
     * false if minimizing.
     * @return The best evaluation score achievable from the current position.
     */
    private int minimax(ChessBoard board, int depth, boolean isMaximizing) {
        if (depth == 0 || board.isCheckmate(board.currentPlayer())) {
            return evaluateBoard(board);
        }

        GameContext.Player currentPlayer = board.currentPlayer();
        List<int[]> legalMoves = board.getAllLegalMoves(currentPlayer);

        int bestScore = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (int[] move : legalMoves) {
            ChessBoard newBoard = board.copy();
            newBoard.movePiece(move[0], move[1], move[2], move[3]);

            int score = minimax(newBoard, depth - 1, !isMaximizing);

            if (isMaximizing) {
                bestScore = Math.max(bestScore, score);
            } else {
                bestScore = Math.min(bestScore, score);
            }
        }

        return bestScore;
    }

    /**
     * Evaluates the given chess board by calculating a simple material score.
     * Sums up the values of all pieces on the board, with positive values
     * favoring White and negative values favoring Black.
     *
     * @param board The chess board to evaluate.
     * @return An integer score representing the material advantage (positive
     * for White, negative for Black).
     */
    private int evaluateBoard(ChessBoard board) {
        int[][] b = board.getBoard();
        int score = 0;

        for (int[] row : b) {
            for (int piece : row) {
                score += getPieceValue(piece);
            }
        }

        return score;
    }

    /**
     * Returns the material value of a chess piece based on its type. Positive
     * values represent White pieces, and negative values represent Black
     * pieces. Piece values are based on standard material weighting (e.g., pawn
     * = 10, queen = 900).
     *
     * @param piece The integer representing the piece (positive for White,
     * negative for Black).
     * @return The material value of the piece (positive if White, negative if
     * Black), or 0 for empty squares.
     */
    private int getPieceValue(int piece) {
        return switch (piece) {
            case 1, -1 ->
                piece > 0 ? 10 : -10;
            case 2, -2 ->
                piece > 0 ? 50 : -50;
            case 3, -3 ->
                piece > 0 ? 30 : -30;
            case 4, -4 ->
                piece > 0 ? 30 : -30;
            case 5, -5 ->
                piece > 0 ? 90 : -90;
            case 6, -6 ->
                piece > 0 ? 900 : -900;
            default ->
                0;
        };
    }
}
