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
 * Enum: ChessPiece
 *
 * This enum represents the various chess pieces and their associated image file names.
 * Each chess piece, including pawns, knights, bishops, rooks, queens, and kings for both 
 * black and white sides, is mapped to a specific image file that is used for rendering 
 * the piece on the chessboard.
 *
 * Key functionalities include:
 * - Providing the file name associated with each chess piece image.
 * - Allowing easy access to chess piece images for rendering purposes.
 */

package com.chess.images;

public enum ChessPiece {
    BOARD("BoardEmpty.png"),
    WHITE_PAWN("PawnW.png"),
    BLACK_PAWN("PawnB.png"),
    WHITE_BISHOP("BishW.png"),
    BLACK_BISHOP("BishB.png"),
    WHITE_KNIGHT("KnightW.png"),
    BLACK_KNIGHT("KnightB.png"),
    WHITE_ROOK("RookW.png"),   
    BLACK_ROOK("RookB.png"),   
    WHITE_QUEEN("QueenW.png"),
    BLACK_QUEEN("QueenB.png"),
    WHITE_KING("KingW.png"),
    BLACK_KING("KingB.png");

    // The file name associated with the chess piece image
    private final String fileName;

    /**
     * Constructor to associate each chess piece with its corresponding image file.
     *
     * @param fileName The name of the image file for the chess piece.
     */
    ChessPiece(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Retrieves the file name associated with the chess piece.
     * 
     * @return The file name as a string (e.g., "PawnW.png").
     */
    public String getFileName() {
        return fileName;
    }
}
