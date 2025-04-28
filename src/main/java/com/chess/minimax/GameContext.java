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
 * Class: GameContext
 *
 * Description:
 * ------------
 * This utility class provides global game state management for the Chess Master application.
 * It tracks the current player's turn, game type, ply count (half-moves), and temporary
 * piece selections for GUI interactions.
 *
 * Key functionalities include:
 * - Managing player turns (White to move or Black to move).
 * - Keeping track of the current game type (Human vs Computer or Computer vs Computer).
 * - Tracking the number of plies (half-moves) that have occurred.
 * - Supporting user piece selection for drag-and-drop functionality in the GUI.
 *
 * Usage:
 * ------
 * - Access or modify the current player using `getCurrentPlayer()` or `setCurrentPlayer(Player)`.
 * - Switch turns using `nextPlayer()`.
 * - Query the game type via `getCurrentGameType()` or update it with `setCurrentGameType(GameType)`.
 *
 * Dependencies:
 * -------------
 * - None. Standalone global context manager.
 *
 * Notes:
 * ------
 * Designed as a final class with a private constructor to prevent instantiation.
 * Intended to be accessed statically throughout the chess engine and GUI components.
 */
package com.chess.minimax;

public final class GameContext {

    /** Tracks the total number of half-moves (plies) made in the game. */
    private static int ply = 0;

    /** 
     * Prevents instantiation of GameContext.
     */
    private GameContext() {
    }

    /**
     * Enum representing the two players in a chess game: WHITE and BLACK.
     */
    public enum Player {
        WHITE, BLACK
    }

    /**
     * Enum representing the type of the chess game.
     */
    public enum GameType {
        HUMAN_VS_COMPUTER, COMPUTER_VS_COMPUTER
    }

    /** The current player whose turn it is to move. */
    private static Player currentPlayer = Player.WHITE;

    /**
     * Gets the current player.
     *
     * @return The player (WHITE or BLACK) who is to move.
     */
    public static Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Sets the current player.
     *
     * @param player The player (WHITE or BLACK) to set as the current player.
     */
    public static void setCurrentPlayer(Player player) {
        currentPlayer = player;
    }

    /** The current type of the game (e.g., human vs computer). */
    private static GameType currentGameType = GameType.COMPUTER_VS_COMPUTER;

    /**
     * Gets the current game type.
     *
     * @return The current game type (HUMAN_VS_COMPUTER or COMPUTER_VS_COMPUTER).
     */
    public static GameType getCurrentGameType() {
        return currentGameType;
    }

    /**
     * Sets the current game type.
     *
     * @param gameType The game type to set (HUMAN_VS_COMPUTER or COMPUTER_VS_COMPUTER).
     */
    public static void setCurrentGameType(GameType gameType) {
        currentGameType = gameType;
    }

    /**
     * Checks if it is White's turn to move.
     *
     * @return True if the current player is White; false otherwise.
     */
    public static boolean isWhiteToMove() {
        return currentPlayer == Player.WHITE;
    }

    /**
     * Checks if it is Black's turn to move.
     *
     * @return True if the current player is Black; false otherwise.
     */
    public static boolean isBlackToMove() {
        return currentPlayer == Player.BLACK;
    }

    /**
     * Switches the turn to the next player and increments the ply counter.
     */
    public static void nextPlayer() {
        currentPlayer = (currentPlayer == Player.WHITE) ? Player.BLACK : Player.WHITE;
        ply++; // Advance one ply (half-move)
        System.out.println(currentPlayer + "'s turn now!");
    }

    /**
     * Gets the current ply (total number of half-moves made).
     *
     * @return The current ply count.
     */
    public static int getPly() {
        return ply;
    }

    /** 
     * Represents the currently selected piece on the board.
     * The array stores the row and column indices; default is {-1, -1} when no piece is selected.
     */
    public static int[] currentPiece = {-1, -1};

}
