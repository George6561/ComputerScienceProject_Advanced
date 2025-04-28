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
 * Class: IntroWindow
 *
 * Description:
 * ------------
 * This class represents the introductory window for the Chess Master application,
 * built using JavaFX. It serves as the main menu, allowing users to select
 * between "Human vs Computer" or "Computer vs Computer" game modes.
 *
 * Key functionalities include:
 * - Displaying a stylized welcome screen with a background image.
 * - Offering interactive buttons for game mode selection.
 * - Styling and handling user interactions through visual effects (hover animations).
 * - Launching the MainWindow based on the selected mode and setting the appropriate GameContext.
 *
 * Usage:
 * ------
 * - Launch `IntroWindow` via JavaFX Application.launch().
 * - Clicking a button will initialize the game mode and transition to the main gameplay window.
 *
 * Dependencies:
 * -------------
 * - JavaFX (Stage, Scene, Layouts, Controls)
 * - GameContext (for setting game type)
 * - MainWindow (the actual gameplay window after selection)
 *
 * Notes:
 * ------
 * This class is lightweight and intended only for initial user interaction.
 * Future improvements could include:
 * - Adding difficulty selection (easy/medium/hard).
 * - Adding theme customization options (light/dark board).
 * - Adding user profile support (e.g., enter player names).
 */
package com.chess.window;

import com.chess.minimax.GameContext;
import com.chess.window.MainWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Objects;

public class IntroWindow extends Application {

    @Override
    public void start(Stage stage) {
        // Background image
        Image backgroundImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/bg.jpg"))); // Place this image in resources/images/
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );

        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new javafx.geometry.Insets(50));
        root.setBackground(new Background(background));

        // Title
        Text title = new Text("Welcome to Chess Master");
        title.setFont(Font.font("Serif", FontWeight.BOLD, 36));
        title.setFill(Color.rgb(47, 9, 5));

        // Buttons
        Button hvcButton = new Button("Human vs Computer");
        Button cvcButton = new Button("Computer vs Computer");

        styleButton(hvcButton);
        styleButton(cvcButton);

        // Add handlers (you can replace with actual game start logic)
        hvcButton.setOnAction(e -> {
            System.out.println("Starting Human vs Computer game...");
            startGame(false, stage);
        });

        cvcButton.setOnAction(e -> {
            System.out.println("Starting Computer vs Computer game...");
            startGame(true, stage);
        });

        root.getChildren().addAll(title, hvcButton, cvcButton);

        Scene scene = new Scene(root, 600, 600);
        stage.setTitle("Chess Master");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Applies consistent styling to a JavaFX Button, including font, colors,
     * cursor behavior, and hover effects to improve the visual appearance and
     * interactivity.
     *
     * @param button The Button to be styled.
     */
    private void styleButton(Button button) {
        button.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 20));
        button.setTextFill(Color.WHITE);
        button.setCursor(Cursor.HAND);
        button.setStyle("-fx-background-color: rgb(47, 9, 5); -fx-background-radius: 12; -fx-padding: 12 24;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: rgb(47, 19, 15); -fx-background-radius: 12; -fx-padding: 12 24;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: rgb(47, 9, 5); -fx-background-radius: 12; -fx-padding: 12 24;"));
    }

    /**
     * Starts a new chess game by setting the game type (human vs computer or
     * computer vs computer), closing the current stage, and launching the main
     * game window.
     *
     * @param cvc True if the game should be computer vs computer; false if
     * human vs computer.
     * @param stage The current JavaFX Stage to be closed before starting the
     * new game window.
     */
    private void startGame(boolean cvc, Stage stage) {
        if (cvc) {
            GameContext.setCurrentGameType(GameContext.GameType.COMPUTER_VS_COMPUTER);
        } else {
            GameContext.setCurrentGameType(GameContext.GameType.HUMAN_VS_COMPUTER);
        }

        stage.close();
        // Now directly launch the MainWindow without calling the main method again
        Platform.runLater(() -> {
            try {
                new MainWindow().start(new Stage()); // create a new stage for the MainWindow
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
