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
 * This class manages the core gameplay loop for the Chess Master application
 * when using the custom Minimax AI engine. It coordinates move generation,
 * board updates, player switching, and GUI refreshing through JavaFX.
 *
 * Key functionalities include:
 * - Alternating turns between White and Black players (Human or AI-controlled).
 * - Integrating with the MinimaxEngine to calculate moves.
 * - Updating the ChessBoard and visual ChessWindow after each move.
 * - Handling pawn promotions and simple move animations (delay).
 *
 * Usage:
 * ------
 * - Instantiate with a ChessWindow and a ChessBoard.
 * - Call `startOnGame()` to begin automated gameplay (Computer vs Computer or Human vs Computer).
 *
 * Dependencies:
 * -------------
 * - ChessBoard (board state management)
 * - ChessWindow (JavaFX GUI rendering)
 * - MinimaxEngine (AI move search)
 * - GameContext (managing player turns and game type)
 *
 * Notes:
 * ------
 * Designed primarily for a simplified Minimax AI. For more advanced AI (e.g., with alpha-beta pruning),
 * consider switching to SearchEngine or StockfishConnector classes.
 */
package com.chess.minimax;

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
     * Initializes a new ChessGame instance with the given window and board
     * state.
     *
     * @param chessWindow The window interface used to display and interact with
     * the game.
     * @param board The initial chess board setup for the game.
     */
    public ChessGame(ChessWindow chessWindow, ChessBoard board) {
        this.chessWindow = chessWindow;
        this.rawMoves = new ArrayList<>();
        this.board = board;
    }

    /**
     * Starts a new game.
     */
    public void startOnGame() {
        displayInitialBoard();

        // Start the game loop for alternating moves between White and Black
        new Thread(() -> {
            while (!board.isCheckmate(GameContext.getCurrentPlayer())) {
//            Platform.runLater(() -> chessWindow.refreshBoard(board));

                if (GameContext.getCurrentGameType() == GameContext.GameType.COMPUTER_VS_COMPUTER) {
                    processMove();
                    GameContext.nextPlayer();
                } else {
                    if (!GameContext.isWhiteToMove()) {
                        processMove();
                        GameContext.nextPlayer();
                    }
                }
            }
        }).start();
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
                Thread.sleep(1500); // 0.5 seconds delay
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
        MinimaxEngine engine = new MinimaxEngine();
        return engine.findBestMove(board, 3); // or depth 4
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
