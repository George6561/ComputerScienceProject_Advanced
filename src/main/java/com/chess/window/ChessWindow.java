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
 * Class: ChessWindow
 *
 * Description:
 * ------------
 * This class represents the main graphical interface for the Chess Master application.
 * It uses JavaFX to render the chessboard, chess pieces, and handle user interactions
 * such as selecting, dragging, and moving pieces.
 *
 * Key functionalities include:
 * - Loading and displaying the chessboard background and piece images dynamically.
 * - Managing mouse interactions: selecting, dragging, and dropping pieces.
 * - Handling special moves like castling and pawn promotion visually and logically.
 * - Refreshing the board state in response to player moves or engine moves.
 * - Managing the JavaFX application lifecycle (startup, window management, closing).
 *
 * Usage:
 * ------
 * - Instantiate with a ChessBoard object to initialize the visual board.
 * - Call `start(Stage primaryStage)` to launch the application window.
 * - Use `displayChessPieces()` or `refreshBoard()` to update piece positions.
 * - Call `movePiece(String move)` to programmatically perform a move.
 *
 * Dependencies:
 * -------------
 * - JavaFX (Scene, Stage, Image, ImageView, Mouse Events)
 * - ChessBoard (internal game state management)
 * - ChessPiece (image mapping based on piece type)
 * - GameContext (to manage current player and turn-based logic)
 *
 * Notes:
 * ------
 * This class is designed for smooth user experience and responsive GUI updates.
 * Future improvements could include:
 * - Animated piece movement.
 * - Highlighting possible moves.
 * - Drag-and-drop feedback (e.g., ghost pieces).
 * - Supporting different board themes and scaling options.
 */
package com.chess.window;

import com.chess.minimax.ChessBoard;
import com.chess.images.ChessPiece;
import com.chess.minimax.GameContext;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

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
 * Class: ChessWindow
 *
 * This class represents the main graphical window for a chess game, implemented
 * using JavaFX. It handles the initialization and display of the chessboard
 * and chess pieces, allowing the user to interact with the game by dragging
 * and dropping pieces. Key functionalities include:
 *
 * - Rendering the chessboard and pieces.
 * - Handling mouse events for selecting, moving, and dragging pieces.
 * - Updating the visual and logical state of the board after each move.
 * - Support for special moves such as castling.
 *
 * The chessboard is represented as an 8x8 grid, and piece images are loaded
 * dynamically based on their positions on the board. The class manages both
 * the internal game state and the visual representation.
 */
public class ChessWindow extends Application {

    // 66x66 pixels for each square
    private final int squareSize = 66;
    // X offset for the board
    private final int offsetX = 18;
    // Y offset for the board
    private final int offsetY = 18;

    // ChessBoard object to manage the internal state of the game
    private ChessBoard chessBoard;

    // JavaFX Scene to represent the game window
    private Scene scene;

    // ImageView to represent the chess piece being dragged by the user
    private ImageView draggingPiece;

    // Coordinates for the piece that is being dragged
    private int draggingFromRow;
    private int draggingFromCol;

    // Boolean to track if a piece is selected
    private boolean isMovingPiece = false;

    private StackPane root = new StackPane();
    private Pane boardPane = new Pane();

    /**
     * Constructs a new ChessWindow instance linked to a specific chess board.
     * The window is responsible for displaying and interacting with the given
     * board state.
     *
     * @param chessBoard The chess board to be displayed and managed by the
     * window.
     */
    public ChessWindow(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
    }

    /**
     * Initializes the primary stage (main window) for the chess game.
     *
     * This method sets up the JavaFX window for the chess game, including: -
     * Loading and displaying the chessboard background. - Initializing the
     * chessboard state and displaying the chess pieces. - Setting up the
     * application's icon and title. - Adding event listeners for handling user
     * interactions (mouse clicks and drags). - Setting up the main scene and
     * preventing the window from being resized.
     *
     * @param primaryStage The main stage (window) for the JavaFX application.
     * @throws Exception If there is an issue loading the chessboard or piece
     * images.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the chessboard image
        InputStream boardImageStream = getClass().getResourceAsStream("/images/BoardEmpty.png");
        if (boardImageStream == null) {
            throw new Exception("Chessboard image not found");
        }

        Image chessBoardImage = new Image(boardImageStream);
        ImageView boardImageView = new ImageView(chessBoardImage);

        // Initialize the chessboard state with a fresh instance
        boardPane.getChildren().add(boardImageView);
        root.getChildren().add(boardPane);

        // Display pieces on the board
        displayChessPieces(-1, -1);

        // Load the icon for the window
        InputStream iconStream = getClass().getResourceAsStream("/images/KnightW.png");
        if (iconStream == null) {
            throw new Exception("Icon image not found");
        }
        Image iconImage = new Image(iconStream);

        scene = new Scene(root, chessBoardImage.getWidth(), chessBoardImage.getHeight());

        // Show the primary stage only if `ChessWindow` is launched as a standalone application
        if (primaryStage != null) {
            primaryStage.setTitle("King Fischer (AI Chess Engine)");
            primaryStage.getIcons().add(iconImage);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        }

        addMouseListeners();
    }

    /**
     * Get the board that is currently displayed in the window.
     *
     * @return The board that is currently displayed in the window.
     */
    public ChessBoard getBoard() {
        if (this.chessBoard == null) {
            throw new IllegalStateException("ChessBoard has not been initialized yet.");
        }
        return this.chessBoard;
    }

    /**
     * Displays all chess pieces on the board, excluding the piece currently
     * being dragged (if any).
     *
     * This method iterates through the chessboard array and renders each chess
     * piece at its respective position on the board. If a piece is being
     * dragged (specified by the excludeRow and excludeCol parameters), that
     * piece is temporarily excluded from the display.
     *
     * - Removes all currently displayed pieces except the board background. -
     * Loads and displays the appropriate image for each piece based on its
     * position.
     *
     * @param excludeRow The row of the piece currently being dragged (exclude
     * from rendering), or -1 if none.
     * @param excludeCol The column of the piece currently being dragged
     * (exclude from rendering), or -1 if none.
     * @throws Exception If the image for any chess piece cannot be found.
     */
    public void displayChessPieces(int excludeRow, int excludeCol) throws Exception {
        int[][] board = chessBoard.getBoard();  // Get the current state of the chessboard

        // Remove all piece images except the board background (which is the first child)
        boardPane.getChildren().removeIf(node -> node instanceof ImageView && node != boardPane.getChildren().get(0));

        // Iterate through each row and column of the board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = board[row][col];  // Get the piece at the current board position

                // If there is a piece at this position and it is not the one being dragged
                if (piece != 0 && !(row == excludeRow && col == excludeCol)) {
                    ChessPiece chessPiece = getChessPiece(piece);  // Get the corresponding ChessPiece enum

                    if (chessPiece != null) {
                        // Load the image for the current piece
                        InputStream pieceImageStream = getClass().getResourceAsStream("/images/" + chessPiece.getFileName());
                        if (pieceImageStream == null) {
                            throw new Exception("Chess piece image not found: " + chessPiece.getFileName());
                        }

                        // Create an Image object and wrap it in an ImageView to display on the board
                        Image pieceImage = new Image(pieceImageStream);
                        ImageView pieceImageView = new ImageView(pieceImage);

                        // Set the position of the piece on the board (account for offsets)
                        pieceImageView.setX(offsetX + col * squareSize);
                        pieceImageView.setY(offsetY + row * squareSize);

                        // Add the piece image to the board
                        boardPane.getChildren().add(pieceImageView);
                    }
                }
            }
        }
    }

    /**
     * Returns the corresponding ChessPiece enum based on the value in the
     * chessboard array.
     *
     * This method maps numerical values from the chessboard array to specific
     * ChessPiece enums. Positive values represent white pieces, and negative
     * values represent black pieces. Each piece type is associated with a
     * specific integer (e.g., 1 for white pawn, -1 for black pawn).
     *
     * @param pieceValue The numerical value representing a chess piece on the
     * board. Positive values represent white pieces, negative values represent
     * black pieces.
     * @return The corresponding ChessPiece enum, or null if the value does not
     * match a valid piece.
     */
    private ChessPiece getChessPiece(int pieceValue) {
        switch (pieceValue) {
            case 1:
                return ChessPiece.WHITE_PAWN;
            case 2:
                return ChessPiece.WHITE_ROOK;
            case 3:
                return ChessPiece.WHITE_KNIGHT;
            case 4:
                return ChessPiece.WHITE_BISHOP;
            case 5:
                return ChessPiece.WHITE_QUEEN;
            case 6:
                return ChessPiece.WHITE_KING;
            case -1:
                return ChessPiece.BLACK_PAWN;
            case -2:
                return ChessPiece.BLACK_ROOK;
            case -3:
                return ChessPiece.BLACK_KNIGHT;
            case -4:
                return ChessPiece.BLACK_BISHOP;
            case -5:
                return ChessPiece.BLACK_QUEEN;
            case -6:
                return ChessPiece.BLACK_KING;
            default:
                return null;  // Return null if the pieceValue doesn't match any valid piece
        }
    }

    /**
     * Executes a move on the chessboard based on a move string and updates the
     * visual display.
     *
     * This method interprets a chess move string in algebraic notation (e.g.,
     * "e2e4") and applies the move on the internal chessboard. It also handles
     * special moves like castling: - "0-0" for kingside castling - "0-0-0" for
     * queenside castling After updating the board's internal state, the display
     * is refreshed to reflect the changes.
     *
     * - Validates the move to ensure it is within bounds and that a piece is
     * present at the source. - Updates both the logical chessboard state and
     * the visual representation of the board.
     *
     * @param move The move in algebraic notation (e.g., "e2e4" for regular
     * moves, "0-0" for castling).
     * @throws Exception If an image cannot be loaded or another error occurs
     * during rendering.
     */
    public void movePiece(String move) throws Exception {
        if (move.equals("0-0")) {  // Kingside castling
            // Move the white king and rook for kingside castling
            chessBoard.movePiece(7, 4, 7, 6);  // King from e1 to g1
            chessBoard.movePiece(7, 7, 7, 5);  // Rook from h1 to f1
            displayChessPieces(-1, -1);  // Redraw all pieces
            //System.out.println("Kingside castling for white.");
        } else if (move.equals("0-0-0")) {  // Queenside castling
            // Move the white king and rook for queenside castling
            chessBoard.movePiece(7, 4, 7, 2);  // King from e1 to c1
            chessBoard.movePiece(7, 0, 7, 3);  // Rook from a1 to d1
            displayChessPieces(-1, -1);  // Redraw all pieces
            //System.out.println("Queenside castling for white.");
        } else {
            // Regular move handling for moves like "e2e4"
            int startX = move.charAt(0) - 'a';  // Convert column letter to index ('a' -> 0, 'b' -> 1, etc.)
            int startY = 8 - Character.getNumericValue(move.charAt(1));  // Convert row number to index (rows are reversed)
            int endX = move.charAt(2) - 'a';
            int endY = 8 - Character.getNumericValue(move.charAt(3));

            // Validate that the move coordinates are within the board boundaries
            if (!isWithinBounds(startX, startY, endX, endY)) {
                //System.out.println("Invalid move coordinates.");
                return;
            }

            // Get the piece from the starting position
            int piece = chessBoard.getBoard()[startY][startX];

            if (piece == 0) {
                // If no piece is found at the source, print an error and exit
                //System.out.println("No piece found at the source.");
                return; // Exit if there's no piece to move
            }

            // Step 1: Update the chessboard's internal state by moving the piece
            chessBoard.movePiece(startY, startX, endY, endX);

            // Step 2: Refresh the board display to reflect the updated positions
            displayChessPieces(-1, -1);  // Redraw all pieces

            //System.out.println("Move made: " + move);
        }
    }

    /**
     * Checks if the provided start and end coordinates are within the bounds of
     * the chessboard.
     *
     * This method ensures that both the starting and ending positions of a move
     * are within the 8x8 grid of the chessboard. It returns true if both
     * coordinates are valid, and false otherwise.
     *
     * @param startX The starting column index (0 to 7) of the move.
     * @param startY The starting row index (0 to 7) of the move.
     * @param endX The ending column index (0 to 7) of the move.
     * @param endY The ending row index (0 to 7) of the move.
     * @return True if the move is within the valid bounds of the chessboard;
     * false otherwise.
     */
    private boolean isWithinBounds(int startX, int startY, int endX, int endY) {
        // Check if all coordinates are within the bounds of 0 to 7 (for an 8x8 chessboard)
        return (startX >= 0 && startX < 8 && startY >= 0 && startY < 8
                && endX >= 0 && endX < 8 && endY >= 0 && endY < 8);
    }

    /**
     * Adds mouse event listeners to the chessboard scene to handle player
     * interactions.
     *
     * This method registers listeners for three types of mouse events: - Press:
     * Detect when the user clicks on a chess piece to select it. - Release:
     * Detect when the user releases the mouse button to drop the piece in a new
     * square. - Drag: Detect when the user drags a piece across the board.
     * These listeners are linked to corresponding handler methods that manage
     * the user's actions.
     */
    private void addMouseListeners() {
        // Add event listeners to the scene for mouse interactions (press, release, drag)
        boardPane.setOnMousePressed(this::handleMousePressed);
        boardPane.setOnMouseReleased(this::handleMouseReleased);
        boardPane.setOnMouseDragged(this::handleMouseDragged);
    }


    /*
     * I will set this to true as I drag the mouse.
     */
    private static boolean moving = false;

    /**
     * The coordinate where the piece begins (x-coordinate).
     */
    private static int startX = 8;

    /**
     * The coordinate where the piece begins (y-coordinate).
     */
    private static int startY = 8;

    /**
     * The coordinate where the piece ends (x-coordinate).
     */
    private static int endX = 8;

    /**
     * The coordinate where the piece ends (y-coordinate).
     */
    private static int endY = 8;

    /**
     * Handles the mouse press event on the chessboard to initiate piece
     * selection.
     *
     * This method is triggered when the user presses the mouse button on the
     * chessboard. It converts the mouse coordinates (sceneX and sceneY) to the
     * corresponding row and column on the chessboard. If no piece is currently
     * being moved, it marks the selected piece's starting position for the
     * upcoming move.
     *
     * - Converts mouse coordinates to chessboard grid coordinates. - Marks the
     * piece's starting position if no piece is currently being moved.
     *
     * @param event The MouseEvent containing information about the mouse press,
     * including its coordinates.
     */
    private void handleMousePressed(MouseEvent event) {
        if (GameContext.isWhiteToMove() && GameContext.getCurrentGameType() == GameContext.GameType.HUMAN_VS_COMPUTER) {
            initialX = (int) (event.getSceneX() - this.offsetX) / this.squareSize;
            initialY = (int) (event.getSceneY() - this.offsetY) / this.squareSize;

            int piece = chessBoard.getPieceAt(initialY, initialX);

            GameContext.currentPiece = new int[]{initialY, initialX};

        }
    }

    /**
     * Handles the mouse release event on the chessboard to finalize the piece's
     * movement.
     *
     * This method is triggered when the user releases the mouse button after
     * dragging a piece. It converts the mouse's release coordinates into
     * chessboard grid coordinates and performs the move if the release occurs
     * on a different square than the starting one. The chessboard state is
     * updated accordingly, and the visual display is refreshed to reflect the
     * move.
     *
     * - Converts mouse release coordinates to board grid coordinates. - Moves
     * the piece to the new position and updates the board both logically and
     * visually. - Ensures the piece is only moved if the release occurs on a
     * different square.
     *
     * @param event The MouseEvent containing information about the mouse
     * release, including its coordinates.
     */
    private void handleMouseReleased(MouseEvent event) {

        if (GameContext.isWhiteToMove() && GameContext.getCurrentGameType() == GameContext.GameType.HUMAN_VS_COMPUTER) {
            deltaX = (int) (event.getSceneX() - this.offsetX) / this.squareSize;
            deltaY = (int) (event.getSceneY() - this.offsetY) / this.squareSize;

            System.out.println(deltaY + ", " + deltaX);

            chessBoard.dragMove(deltaY, deltaX);
            Platform.runLater(() -> refreshBoard(chessBoard));

        }

    }

    /**
     * Handles the mouse drag event on the chessboard.
     *
     * This method is triggered when the user drags a chess piece across the
     * board after selecting it. Currently, it outputs a message indicating that
     * a piece is being dragged, but it can be extended to visually move the
     * piece along with the mouse cursor during the drag action.
     *
     * - Outputs a message to indicate the dragging action. - Can be extended to
     * implement more complex dragging behavior (e.g., visual feedback).
     *
     * @param event The MouseEvent containing information about the mouse drag,
     * including its coordinates.
     */
    private void handleMouseDragged(MouseEvent event) {

    }

    private int initialX;
    private int initialY;

    private int deltaX;
    private int deltaY;

    /**
     * Closes the window programmatically.
     *
     * This method retrieves the Stage from the current scene and closes it,
     * effectively ending the JavaFX application if this is the primary stage.
     */
    public void closeWindow() {
        Platform.runLater(() -> {
            if (this.scene != null && this.scene.getWindow() != null) {
                this.scene.getWindow().hide();
            } else {
                System.out.println("Scene or its window is null, cannot close window.");
            }
        });
    }

    /**
     * Retrieves the JavaFX Scene associated with this chess window. The Scene
     * contains all visual components for displaying the chess board and pieces.
     *
     * @return The Scene object representing the game's main interface.
     */
    public Scene getScene() {
        return this.scene;
    }

    /**
     * Refreshes the visual display of the chess board based on the current
     * board state. Clears any existing piece images and redraws all pieces in
     * their updated positions. This method is executed on the JavaFX
     * Application Thread to ensure thread safety.
     *
     * @param board The current chess board whose state will be visually
     * rendered.
     */
    public void refreshBoard(ChessBoard board) {
        Platform.runLater(() -> {
            // Get the current board state
            int[][] currentBoard = board.getBoard();

            // Remove all existing pieces on the board pane
            boardPane.getChildren().removeIf(node -> node instanceof ImageView && node != boardPane.getChildren().get(0));

            // Loop through the board and place each piece on the correct square
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    int piece = currentBoard[row][col];
                    if (piece != 0) { // If there's a piece at this position
                        ChessPiece chessPiece = getChessPiece(piece);

                        if (chessPiece != null) {
                            try {
                                // Load the image for the chess piece
                                InputStream pieceImageStream = getClass().getResourceAsStream("/images/" + chessPiece.getFileName());
                                if (pieceImageStream == null) {
                                    throw new Exception("Chess piece image not found: " + chessPiece.getFileName());
                                }

                                // Create an Image and ImageView for the piece
                                Image pieceImage = new Image(pieceImageStream);
                                ImageView pieceImageView = new ImageView(pieceImage);

                                // Set the position of the piece on the board
                                pieceImageView.setX(offsetX + col * squareSize);
                                pieceImageView.setY(offsetY + row * squareSize);

                                // Add the piece to the board pane
                                boardPane.getChildren().add(pieceImageView);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }

}
