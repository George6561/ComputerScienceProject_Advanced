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
 * Class: Main
 *
 * Description:
 * ------------
 * This class serves as the entry point for the Chess Master application.
 * It initializes and launches the JavaFX GUI by invoking the IntroWindow,
 * where users can select the game mode (e.g., Human vs Computer or Computer vs Computer).
 *
 * Key functionalities include:
 * - Starting the JavaFX application lifecycle.
 * - Launching the initial intro screen.
 *
 * Usage:
 * ------
 * - Run this class to start the chess application.
 * - The JavaFX platform will be initialized and the user interface will be presented.
 *
 * Dependencies:
 * -------------
 * - JavaFX Application Toolkit (for UI rendering and event handling)
 * - IntroWindow (initial menu window)
 * - MainWindow (main gameplay window after selection)
 *
 * Notes:
 * ------
 * Ensure JavaFX libraries are properly configured in the project's runtime environment.
 */

package com.chess.main;

import com.chess.window.IntroWindow;
import com.chess.window.MainWindow;
import javafx.application.Application;

/**
 * Entry point for the chess application. Launches the JavaFX GUI.
 */
public class Main {

    public static void main(String[] args) {
        Application.launch(IntroWindow.class, args);
        // debugOpeningMoves();
    }


}
