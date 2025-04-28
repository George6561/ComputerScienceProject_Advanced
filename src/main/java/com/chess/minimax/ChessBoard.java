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
 * Class: ChessBoard
 *
 * This class represents a chessboard and provides methods to manage its state.
 * The chessboard is stored as an 8x8 integer array, where positive numbers represent 
 * white pieces and negative numbers represent black pieces.
 *
 * Key functionalities include:
 * - Retrieving the board state as a 2D array or a 1D array.
 * - Moving pieces on the board, handling special moves such as castling.
 * - Adding and removing pieces from specific positions on the board.
 * - Managing player turns and determining the current player.
 */
package com.chess.minimax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChessBoard {

    private int lastCapturedPiece = 0;

    // The chessboard is represented as an 8x8 2D array.
    private final int[][] board = new int[][]{
        {-2, -3, -4, -5, -6, -4, -3, -2}, // Row 0: Black's major pieces
        {-1, -1, -1, -1, -1, -1, -1, -1}, // Row 1: Black's pawns
        {0, 0, 0, 0, 0, 0, 0, 0}, // Empty squares
        {0, 0, 0, 0, 0, 0, 0, 0}, // Empty squares
        {0, 0, 0, 0, 0, 0, 0, 0}, // Empty squares
        {0, 0, 0, 0, 0, 0, 0, 0}, // Empty squares
        {1, 1, 1, 1, 1, 1, 1, 1}, // White's pawns
        {2, 3, 4, 5, 6, 4, 3, 2} // Row 7: White's major pieces
    };

    private int[] lastMove;

    //Required for making special moves
    private boolean whiteKingMoved = false;
    private boolean whiteLeftRookMoved = false;
    private boolean whiteRightRookMoved = false;
    private boolean blackKingMoved = false;
    private boolean blackLeftRookMoved = false;
    private boolean blackRightRookMoved = false;

    private int[] enPassantSquare = null;

    private static final Map<Character, Integer> PIECE_MAP = new HashMap<>();

    static {
        PIECE_MAP.put('p', -1);
        PIECE_MAP.put('r', -2);
        PIECE_MAP.put('n', -3);
        PIECE_MAP.put('b', -4);
        PIECE_MAP.put('q', -5);
        PIECE_MAP.put('k', -6);
        PIECE_MAP.put('P', 1);
        PIECE_MAP.put('R', 2);
        PIECE_MAP.put('N', 3);
        PIECE_MAP.put('B', 4);
        PIECE_MAP.put('Q', 5);
        PIECE_MAP.put('K', 6);
    }

    /**
     * Create a blank chess board.
     */
    public ChessBoard() {

    }

    /**
     * Create a chessboard from the FEN layout.
     *
     * @param fen FEN layout of the board.
     */
    public ChessBoard(String fen) {
        String[] parts = fen.split(" ");
        String boardPart = parts[0];
        String[] ranks = boardPart.split("/");

        for (int row = 0; row < 8; row++) {
            int col = 0;
            for (char c : ranks[7 - row].toCharArray()) {
                if (Character.isDigit(c)) {
                    col += Character.getNumericValue(c);
                } else {
                    board[row][col++] = PIECE_MAP.getOrDefault(c, 0);
                }
            }
        }
    }

    /**
     * Returns a copy of the current chessboard as a 2D array.
     *
     * This method returns a deep copy of the 2D array to prevent direct
     * modification of the internal board state by other classes.
     *
     * @return A copy of the chessboard as a 2D integer array.
     */
    public int[][] getBoard() {
        int[][] boardCopy = new int[board.length][];
        for (int i = 0; i < board.length; i++) {
            boardCopy[i] = board[i].clone(); // Clone each row for deep copy
        }
        return boardCopy;
    }

    /**
     * Moves a piece from one square to another on the chessboard.
     *
     * Special moves like castling are handled separately.
     *
     * @param fromRow The starting row of the piece.
     * @param fromCol The starting column of the piece.
     * @param toRow The destination row of the piece.
     * @param toCol The destination column of the piece.
     */
    public void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        int movingPiece = board[fromRow][fromCol];

        // Track castling rights
        if (movingPiece == 6) {
            whiteKingMoved = true;
        }
        if (movingPiece == -6) {
            blackKingMoved = true;
        }
        if (fromRow == 7 && fromCol == 0) {
            whiteLeftRookMoved = true;
        }
        if (fromRow == 7 && fromCol == 7) {
            whiteRightRookMoved = true;
        }
        if (fromRow == 0 && fromCol == 0) {
            blackLeftRookMoved = true;
        }
        if (fromRow == 0 && fromCol == 7) {
            blackRightRookMoved = true;
        }

        // Track en passant opportunity
        if (Math.abs(movingPiece) == 1 && Math.abs(fromRow - toRow) == 2) {
            enPassantSquare = new int[]{(fromRow + toRow) / 2, fromCol};
        } else {
            enPassantSquare = null;
        }

        // Handle normal move
        lastCapturedPiece = board[toRow][toCol];
        board[fromRow][fromCol] = 0;
        board[toRow][toCol] = movingPiece;

        lastMove = new int[]{fromRow, fromCol, toRow, toCol};
    }

    /**
     * Adds all possible king moves from a given position to the provided move
     * list. Includes standard king movements (one square in any direction) and
     * castling moves, checking for valid moves and friendly pieces.
     *
     * @param row The current row position of the king.
     * @param col The current column position of the king.
     * @param isWhite True if the king is white; false if the king is black.
     * @param moves The list to which valid king moves will be added.
     */
    private void addKingMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        int[][] kingMoves = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (int[] move : kingMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            if (isValidMove(newRow, newCol) && !isFriendlyPiece(newRow, newCol, isWhite)) {
                moves.add(new int[]{row, col, newRow, newCol});
            }
        }

        // Handle castling
        if (isWhite && !whiteKingMoved) {
            if (!whiteRightRookMoved && board[7][5] == 0 && board[7][6] == 0) {
                if (!isInCheck(GameContext.Player.WHITE)) {
                    moves.add(new int[]{7, 4, 7, 6}); // King-side castle
                }
            }
            if (!whiteLeftRookMoved && board[7][1] == 0 && board[7][2] == 0 && board[7][3] == 0) {
                if (!isInCheck(GameContext.Player.WHITE)) {
                    moves.add(new int[]{7, 4, 7, 2}); // Queen-side castle
                }
            }
        }

        if (!isWhite && !blackKingMoved) {
            if (!blackRightRookMoved && board[0][5] == 0 && board[0][6] == 0) {
                if (!isInCheck(GameContext.Player.BLACK)) {
                    moves.add(new int[]{0, 4, 0, 6});
                }
            }
            if (!blackLeftRookMoved && board[0][1] == 0 && board[0][2] == 0 && board[0][3] == 0) {
                if (!isInCheck(GameContext.Player.BLACK)) {
                    moves.add(new int[]{0, 4, 0, 2});
                }
            }
        }
    }

    /**
     * Returns the last move made in the game.
     *
     * @return An array representing the last move, or null if no moves have
     * been made.
     */
    public int[] getLastMove() {
        return lastMove;
    }

    /**
     * Promotes a pawn that reaches the end of the board.
     *
     * @param row The row where the pawn is located.
     * @param col The column where the pawn is located.
     * @param isWhite True if the pawn is white, false if black.
     */
    private void promotePawn(int row, int col, boolean isWhite) {
        String choice = "Q";  // Automatically promote to a queen for now

        int newPiece;
        switch (choice) {
            case "Q" ->
                newPiece = isWhite ? 5 : -5;  // Queen
            case "R" ->
                newPiece = isWhite ? 2 : -2;  // Rook
            case "N" ->
                newPiece = isWhite ? 3 : -3;  // Knight
            case "B" ->
                newPiece = isWhite ? 4 : -4;  // Bishop
            default ->
                newPiece = isWhite ? 5 : -5;  // Default to Queen
        }

        board[row][col] = newPiece;
    }

    /**
     * Removes a piece from the specified square on the chessboard.
     *
     * @param row The row of the piece to remove.
     * @param col The column of the piece to remove.
     */
    public void removePiece(int row, int col) {
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            board[row][col] = 0;  // Set the square to empty (0)
        }
    }

    /**
     * Adds a piece to the specified square on the chessboard.
     *
     * @param row The row where the piece will be placed.
     * @param col The column where the piece will be placed.
     * @param piece The piece to add (use positive values for white, negative
     * for black).
     */
    public void addPiece(int row, int col, int piece) {
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            board[row][col] = piece;  // Place the piece on the board
        }
    }

    /**
     * Switches the turn to the next player (White to Black or Black to White).
     */
    public void nextMove() {
        if (GameContext.getCurrentPlayer() == GameContext.Player.WHITE) {
            GameContext.setCurrentPlayer(GameContext.Player.BLACK);
        } else {
            GameContext.setCurrentPlayer(GameContext.Player.WHITE);
        }
    }

    /**
     * Determines which player is currently allowed to move.
     *
     * @return The current player (WHITE or BLACK).
     */
    public GameContext.Player currentPlayer() {
        return GameContext.getCurrentPlayer();
    }

    /**
     * Returns a string representation of the chessboard.
     *
     * @return A string representing the current state of the chessboard.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = board[row][col];
                switch (piece) {
                    case -1 ->
                        sb.append('p');  // Black Pawn
                    case -2 ->
                        sb.append('r');  // Black Rook
                    case -3 ->
                        sb.append('n');  // Black Knight
                    case -4 ->
                        sb.append('b');  // Black Bishop
                    case -5 ->
                        sb.append('q');  // Black Queen
                    case -6 ->
                        sb.append('k');  // Black King
                    case 1 ->
                        sb.append('P');   // White Pawn
                    case 2 ->
                        sb.append('R');   // White Rook
                    case 3 ->
                        sb.append('N');   // White Knight
                    case 4 ->
                        sb.append('B');   // White Bishop
                    case 5 ->
                        sb.append('Q');   // White Queen
                    case 6 ->
                        sb.append('K');   // White King
                    default ->
                        sb.append('*');  // Empty square
                }
            }
            sb.append('\n');  // New line after each row
        }
        return sb.toString();
    }

    /**
     * Returns all legal moves for the current player. Each move is represented
     * as an array: [fromRow, fromCol, toRow, toCol].
     *
     * @param player The player whose moves are being calculated.
     * @return A list of arrays representing legal moves for the current player.
     */
    public List<int[]> getAllLegalMoves(GameContext.Player player) {
        List<int[]> legalMoves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = board[row][col];
                if ((player == GameContext.Player.WHITE && piece > 0)
                        || (player == GameContext.Player.BLACK && piece < 0)) {

                    List<int[]> pieceMoves = getMovesForPiece(row, col, piece);

                    for (int[] move : pieceMoves) {
                        int capturedPiece = board[move[2]][move[3]];
                        movePiece(move[0], move[1], move[2], move[3]);

                        boolean stillInCheck = isInCheck(player);

                        // Undo the move
                        board[move[0]][move[1]] = piece;
                        board[move[2]][move[3]] = capturedPiece;

                        if (!stillInCheck) {
                            legalMoves.add(move);
                        }
                    }
                }
            }
        }

        return legalMoves;
    }

    /**
     * Returns a list of potential moves for a given piece. This function does
     * not check for checks or pins.
     *
     * @param row The row of the piece.
     * @param col The column of the piece.
     * @param piece The piece to find moves for.
     * @return A list of arrays representing potential moves for the piece.
     */
    private List<int[]> getMovesForPiece(int row, int col, int piece) {
        List<int[]> moves = new ArrayList<>();
        boolean isWhite = piece > 0;
        switch (Math.abs(piece)) {
            case 1 ->
                addPawnMoves(row, col, isWhite, moves);  // Pawn
            case 2 ->
                addRookMoves(row, col, isWhite, moves);  // Rook
            case 3 ->
                addKnightMoves(row, col, isWhite, moves);  // Knight
            case 4 ->
                addBishopMoves(row, col, isWhite, moves);  // Bishop
            case 5 ->
                addQueenMoves(row, col, isWhite, moves);  // Queen
            case 6 ->
                addKingMoves(row, col, isWhite, moves);  // King
        }
        return moves;
    }

    /**
     * Retrieves all possible legal moves for the piece located at the specified
     * position.
     *
     * @param row The row index of the piece on the board.
     * @param col The column index of the piece on the board.
     * @return A list of possible moves for the piece, where each move is
     * represented as an array of integers.
     */
    public List<int[]> getMovesForPiece(int row, int col) {
        return getMovesForPiece(row, col, board[row][col]);
    }

    // Helper methods for each type of piece
    /**
     * Adds possible pawn moves to the moves list.
     *
     * @param row The row of the pawn.
     * @param col The column of the pawn.
     * @param isWhite True if the pawn is white, false if black.
     * @param moves The list to add the moves to.
     */
    private void addPawnMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        int direction = isWhite ? -1 : 1;
        int startRow = isWhite ? 6 : 1;

        // Normal advance
        if (isValidMove(row + direction, col) && board[row + direction][col] == 0) {
            moves.add(new int[]{row, col, row + direction, col});
            if (row == startRow && board[row + 2 * direction][col] == 0) {
                moves.add(new int[]{row, col, row + 2 * direction, col});
            }
        }

        // Captures
        for (int dcol = -1; dcol <= 1; dcol += 2) {
            int newCol = col + dcol;
            if (isValidMove(row + direction, newCol) && isOpponentPiece(row + direction, newCol, isWhite)) {
                moves.add(new int[]{row, col, row + direction, newCol});
            }

            // En passant
            if (enPassantSquare != null && enPassantSquare[0] == row + direction && enPassantSquare[1] == newCol) {
                moves.add(new int[]{row, col, row + direction, newCol});
            }
        }

        // Promote pawn if reaching last rank (handle during move execution)
    }

    /**
     * Adds possible rook moves to the moves list.
     *
     * @param row The row of the rook.
     * @param col The column of the rook.
     * @param isWhite True if the rook is white, false if black.
     * @param moves The list to add the moves to.
     */
    private void addRookMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        addLinearMoves(row, col, isWhite, moves, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
    }

    /**
     * Adds possible knight moves to the moves list.
     *
     * @param row The row of the knight.
     * @param col The column of the knight.
     * @param isWhite True if the knight is white, false if black.
     * @param moves The list to add the moves to.
     */
    private void addKnightMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        int[][] knightMoves = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
        for (int[] move : knightMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            if (isValidMove(newRow, newCol) && !isFriendlyPiece(newRow, newCol, isWhite)) {
                moves.add(new int[]{row, col, newRow, newCol});
            }
        }
    }

    /**
     * Adds possible bishop moves to the moves list.
     *
     * @param row The row of the bishop.
     * @param col The column of the bishop.
     * @param isWhite True if the bishop is white, false if black.
     * @param moves The list to add the moves to.
     */
    private void addBishopMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        addLinearMoves(row, col, isWhite, moves, new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}});
    }

    /**
     * Adds possible queen moves to the moves list.
     *
     * @param row The row of the queen.
     * @param col The column of the queen.
     * @param isWhite True if the queen is white, false if black.
     * @param moves The list to add the moves to.
     */
    private void addQueenMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        addLinearMoves(row, col, isWhite, moves, new int[][]{
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}});
    }

    /**
     * Adds linear moves in specified directions for a piece.
     *
     * @param row The starting row of the piece.
     * @param col The starting column of the piece.
     * @param isWhite True if the piece is white, false if black.
     * @param moves The list to add the moves to.
     * @param directions The array of directions to add moves for.
     */
    private void addLinearMoves(int row, int col, boolean isWhite, List<int[]> moves, int[][] directions) {
        for (int[] direction : directions) {
            int newRow = row + direction[0];
            int newCol = col + direction[1];
            while (isValidMove(newRow, newCol) && !isFriendlyPiece(newRow, newCol, isWhite)) {
                moves.add(new int[]{row, col, newRow, newCol});
                if (board[newRow][newCol] != 0) { // Stop at the first opponent piece
                    break;
                }
                newRow += direction[0];
                newCol += direction[1];
            }
        }
    }

    /**
     * Checks if the given coordinates are within the bounds of the chessboard.
     *
     * @param row The row index to check.
     * @param col The column index to check.
     * @return True if the coordinates are valid, false otherwise.
     */
    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    /**
     * Determines if a piece at the given coordinates is friendly to the current
     * player.
     *
     * @param row The row index of the piece.
     * @param col The column index of the piece.
     * @param isWhite True if checking for a white piece, false for a black
     * piece.
     * @return True if the piece is friendly, false otherwise.
     */
    private boolean isFriendlyPiece(int row, int col, boolean isWhite) {
        int piece = board[row][col];
        return (isWhite && piece > 0) || (!isWhite && piece < 0);
    }

    /**
     * Determines if a piece at the given coordinates is an opponent's piece.
     *
     * @param row The row index of the piece.
     * @param col The column index of the piece.
     * @param isWhite True if checking for a white piece's opponent, false for
     * black.
     * @return True if the piece is an opponent's piece, false otherwise.
     */
    private boolean isOpponentPiece(int row, int col, boolean isWhite) {
        int piece = board[row][col];
        return (isWhite && piece < 0) || (!isWhite && piece > 0);
    }

    /**
     * Checks if the king of the specified player is in check.
     *
     * @param player The player to check (WHITE or BLACK).
     * @return True if the king is in check, false otherwise.
     */
    public boolean isInCheck(GameContext.Player player) {
        int kingRow = -1, kingCol = -1;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = board[row][col];
                if ((player == GameContext.Player.WHITE && piece == 6)
                        || (player == GameContext.Player.BLACK && piece == -6)) {
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
            if (kingRow != -1) {
                break;
            }
        }

        GameContext.Player opponent = (player == GameContext.Player.WHITE)
                ? GameContext.Player.BLACK
                : GameContext.Player.WHITE;

        List<int[]> opponentMoves = getAllPotentialMoves(opponent);

        for (int[] move : opponentMoves) {
            if (move[2] == kingRow && move[3] == kingCol) {
                return true;  // The king is in check
            }
        }

        return false;
    }

    /**
     * Generates all potential legal moves for the specified player based on the
     * current board state.
     *
     * @param player The player (WHITE or BLACK) whose possible moves are to be
     * calculated.
     * @return A list of all potential moves for the player, where each move is
     * represented as an array of integers.
     */
    public List<int[]> getAllPotentialMoves(GameContext.Player player) {
        List<int[]> potentialMoves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = board[row][col];
                if ((player == GameContext.Player.WHITE && piece > 0)
                        || (player == GameContext.Player.BLACK && piece < 0)) {
                    potentialMoves.addAll(getMovesForPiece(row, col, piece));
                }
            }
        }
        return potentialMoves;
    }

    /**
     * Checks if the specified player is in checkmate.
     *
     * @param player The player to check (WHITE or BLACK).
     * @return True if the player is in checkmate, false otherwise.
     */
    public boolean isCheckmate(GameContext.Player player) {
        // First, check if the king is in check
        if (!isInCheck(player)) {
            return false; // If the king is not in check, it can't be checkmate
        }

        // Get all legal moves for the player
        List<int[]> legalMoves = getAllLegalMoves(player);

        // If there are no legal moves left, the player is in checkmate
        return legalMoves.isEmpty();
    }

    /**
     * Returns the piece located at the specified position on the board.
     *
     * @param row The row index (0-7) of the piece.
     * @param col The column index (0-7) of the piece.
     * @return The integer value representing the piece at the given position,
     * or 0 if the position is empty.
     */
    public int getPieceAt(int row, int col) {
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            return board[row][col];
        } else {
            throw new IllegalArgumentException("Position out of bounds");
        }
    }

    /**
     * Creates and returns a deep copy of the current chess board.
     *
     * @return A new ChessBoard object with identical piece positions to the
     * current board.
     */
    public ChessBoard copy() {
        ChessBoard newBoard = new ChessBoard();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                newBoard.board[i][j] = this.board[i][j];
            }
        }
        return newBoard;
    }

    /**
     * Computes a hash value representing the current state of the chess board,
     * including piece positions and which player's turn it is to move.
     *
     * @return A long integer hash uniquely identifying the current board state.
     */
    public long hash() {
        long hash = 0;
        int[][] b = this.getBoard();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = b[row][col];
                hash ^= Objects.hash(piece, row, col);
            }
        }

        // Include the current player in the hash
        hash ^= GameContext.isWhiteToMove() ? 1L : 2L;

        return hash;
    }

    /**
     * Attempts to move the currently selected piece to the specified
     * destination if the move is valid. Executes the move and advances the turn
     * to the next player if the move is legal.
     *
     * @param newRow The target row to move the piece to.
     * @param newCol The target column to move the piece to.
     */
    public void dragMove(int newRow, int newCol) {
        List<int[]> moves = getMovesForPiece(GameContext.currentPiece[0], GameContext.currentPiece[1]);
        System.out.print("Move requested from: [row: " + GameContext.currentPiece[0] + ", col: " + GameContext.currentPiece[1] + "]");
        System.out.println(" To: [row: " + newRow + ", col: " + newCol + "]");
        for (int[] move : moves) {
            if (newRow == move[2] && newCol == move[3]) {
                movePiece(GameContext.currentPiece[0], GameContext.currentPiece[1], newRow, newCol);
                GameContext.nextPlayer();
            }
        }
    }

}
