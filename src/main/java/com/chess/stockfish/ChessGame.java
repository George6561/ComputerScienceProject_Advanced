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
 * Class: ChessGame
 *
 * Description:
 * ------------
 * This class manages the main game loop for the Chess Master application
 * when playing autonomously (Computer vs Computer) using a custom Minimax/Monte Carlo engine.
 * It handles move generation, board updates, and GUI refreshing via JavaFX.
 *
 * Key functionalities include:
 * - Starting and running a complete automated chess game.
 * - Integrating with a SearchEngine to find optimal moves.
 * - Applying moves and handling special cases like pawn promotions.
 * - Updating the visual ChessWindow interface during gameplay.
 *
 * Usage:
 * ------
 * - Instantiate with a ChessWindow.
 * - Call `startOneGame()` to launch a complete chess match between AI players.
 *
 * Dependencies:
 * -------------
 * - ChessBoard (board representation and move handling)
 * - ChessWindow (JavaFX-based GUI display)
 * - Move (move encoding and handling)
 * - SearchEngine (AI move searching with alpha-beta pruning)
 * - GameContext (managing player turns and move tracking)
 *
 * Notes:
 * ------
 * Currently uses a fixed search depth for move generation.
 * Future improvements could include:
 * - Adjustable depth based on difficulty level or time control.
 * - Multi-threaded or asynchronous search for smoother GUI performance.
 * - Integration with external engines like Stockfish for hybrid play.
 */
package com.chess.stockfish;

import com.chess.minimax.ChessBoard;
import com.chess.minimax.GameContext;
import com.chess.montecarlo.Move;
import com.chess.montecarlo.SearchEngine;
import com.chess.window.ChessWindow;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * ChessGame class where both White and Black use the custom Minimax engine.
 */
public class ChessGame {

    private ChessWindow chessWindow;
    private List<String> rawMoves;
    private boolean isWhiteToMove = true;
    private ChessBoard board;

    /**
     * Constructs a new ChessGame instance with a given display window.
     * Initializes an empty list for move history and sets up a fresh chess
     * board.
     *
     * @param chessWindow The window interface used for displaying and
     * interacting with the chess game.
     */
    public ChessGame(ChessWindow chessWindow) {
        this.chessWindow = chessWindow;
        this.rawMoves = new ArrayList<>();
        this.board = new ChessBoard();
    }

    /**
     * Starts a new game.
     */
    public void startOneGame() {
        displayInitialBoard();

        // Start the game loop for alternating moves between White and Black
        while (!board.isCheckmate(GameContext.getCurrentPlayer())) {
            // Display the board
            Platform.runLater(() -> chessWindow.refreshBoard(board));

            // Process the move for the current player (White or Black)
            processMove();

            // Switch turn
            GameContext.nextPlayer();
        }
    }

    /**
     * Displays the initial board in the chess window.
     */
    private void displayInitialBoard() {
        Platform.runLater(() -> {
            try {
                chessWindow.displayChessPieces(-1, -1); // Display pieces in the initial state
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Process the move for the current player using the Minimax engine.
     */
    private void processMove() {
        // Search for the best move for the current player
        Move bestMove = getBestMove();
        if (bestMove != null) {
            // Apply the best move to the board
            applyMove(bestMove);

            // Optionally, we could add a delay to show the move animation or visual update
            try {
                Thread.sleep(500); // 0.5 seconds delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Finds the best move for the current player using the Minimax engine. This
     * is a placeholder for your SearchEngine's findBestMove method.
     *
     * @return The best move for the current player.
     */
    private Move getBestMove() {
        // For now, we're just returning a mock move. Replace with actual Minimax logic.
        SearchEngine searchEngine = new SearchEngine();
        return searchEngine.findBestMove(board, 4); // Search depth 4 for example
    }

    /**
     * Applies the given move to the chessboard.
     *
     * @param move The move to apply.
     */
    private void applyMove(Move move) {
        int fromRow = move.getFrom() / 8;
        int fromCol = move.getFrom() % 8;
        int toRow = move.getTo() / 8;
        int toCol = move.getTo() % 8;

        board.movePiece(fromRow, fromCol, toRow, toCol);

        // If it's a promotion move, handle the promotion (default to Queen for now)
        if (move.isPromotionMove()) {
            board.addPiece(toRow, toCol, GameContext.isWhiteToMove() ? 5 : -5); // Queen promotion
        }

        // Update the UI with the new board state
        Platform.runLater(() -> chessWindow.refreshBoard(board));
    }
}
