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
 * Class: MainWindow
 *
 * This class serves as the main entry point for the graphical user interface
 * of the chess application. It initializes the application window, manages the
 * layout, and handles the lifecycle of individual chess games.
 *
 * Key functionalities include:
 * - Setting up the JavaFX `Stage` with a custom icon and layout.
 * - Starting and restarting chess games using `ChessWindow` and `ChessGame`.
 * - Managing the end of a game and transitioning to a new game.
 *
 * Dependencies:
 * - `ChessWindow` for rendering the chessboard and game interface.
 * - `ChessGame` for managing the chess game logic and engines.
 * - JavaFX `Application` for creating the graphical interface.
 *
 * Usage:
 * - Launch the application by calling the `main` method in the `Main` class.
 * - This class is automatically initialized as part of the JavaFX lifecycle.
 */
package com.chess.window;

import com.chess.minimax.ChessBoard;
import com.chess.minimax.ChessGame;

import java.io.InputStream;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainWindow extends Application {

    private BorderPane mainLayout;            // Main layout container for the application
    private ChessWindow currentChessWindow;   // Current chess game window
    private ChessGame currentChessGame;       // Current chess game logic and state
    private ChessBoard chessBoard;

    /**
     * Starts the JavaFX application and initializes the main window.
     *
     * This method sets up the primary `Stage`, including the layout, custom
     * icon, and the first chess game. It is called automatically by the JavaFX
     * runtime when the application is launched.
     *
     * @param primaryStage The primary `Stage` for the application.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Window is closing. Shutting down...");
            Platform.exit();         // Shut down JavaFX application thread
            System.exit(0);          // Force exit JVM (kills all other threads)
        });

        // Set the custom icon for the application (Knight logo)
        InputStream iconStream = getClass().getResourceAsStream("/images/KnightW.png");
        if (iconStream != null) {
            Image iconImage = new Image(iconStream);
            primaryStage.getIcons().add(iconImage);
        } else {
            System.out.println("Icon image not found.");
        }

        // Initialize the main container layout
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: rgb(47, 9, 5);");
        primaryStage.setWidth(564 + 14);
        primaryStage.setHeight(564 + 39);
        primaryStage.setResizable(false);

        // Set up and display the first chess game
        chessBoard = new ChessBoard();
        startNewGame();

        // Create the main scene and configure the primary stage
        Scene scene = new Scene(mainLayout, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("King Fischer (AI Chess Engine)");
        primaryStage.show();
    }

    /**
     * Starts a new chess game by initializing a new `ChessWindow` and
     * `ChessGame`.
     *
     * This method ensures any existing chess game and UI components are
     * properly closed and replaced with a fresh game. It also adds a listener
     * to handle game-ending events, such as restarting the game.
     */
    private void startNewGame() {
        Platform.runLater(() -> {
            try {
                // Close the current chess window if one exists
                if (currentChessWindow != null) {
                    currentChessWindow.closeWindow();
                }

                // Create and initialize a new ChessWindow
                currentChessWindow = new ChessWindow(chessBoard);
                currentChessWindow.start(null);

                // Initialize the ChessGame with the new ChessWindow
                currentChessGame = new ChessGame(currentChessWindow, chessBoard);

                // Start the chess game in a separate thread
                Thread gameThread = new Thread(() -> {
                    currentChessGame.startOnGame();
                });
                gameThread.setDaemon(true);
                gameThread.start();

                // Wrap the chessboard UI in a layout with padding
                BorderPane chessBoardWrapper = new BorderPane();
                chessBoardWrapper.setPadding(new Insets(0, 0, 0, 0));
                chessBoardWrapper.setCenter(currentChessWindow.getScene().getRoot());

                // Set the wrapped chessboard as the center of the main layout
                mainLayout.setCenter(chessBoardWrapper);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
