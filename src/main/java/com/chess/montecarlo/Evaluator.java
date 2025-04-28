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
 * Class: Evaluator
 *
 * Description:
 * ------------
 * This class provides a sophisticated board evaluation function for the Chess Master application.
 * It combines material evaluation, piece-square tables (PSQT), king safety heuristics,
 * tempo bonuses, and early-game positional incentives.
 *
 * Key functionalities include:
 * - Material counting based on predefined piece values.
 * - Dynamic evaluation using Midgame (MG) and Endgame (EG) piece-square tables.
 * - Detection and rewarding of positional features such as:
 *   - Center control
 *   - Pawn structure
 *   - Early knight development
 *   - Pawn shields for king safety
 *   - Tempo advantage and mobility
 * - Gradual blending of midgame and endgame evaluation based on the phase of the game.
 *
 * Usage:
 * ------
 * - Call `evaluate(board)` to receive an integer score representing the favorability
 *   of the position for the current player.
 *
 * Dependencies:
 * -------------
 * - ChessBoard (board state and piece positioning)
 * - GameContext (to retrieve current player and ply count)
 *
 * Notes:
 * ------
 * This evaluator uses a basic but effective model inspired by Stockfish-style heuristics,
 * making it suitable for a strong amateur-level chess engine.
 * Further improvements could include:
 * - King tropism
 * - Pawn structure complexity (isolated, doubled, passed pawns)
 * - Rook open-file bonuses
 * - Advanced king activity in endgames.
 */
package com.chess.montecarlo;

import com.chess.minimax.ChessBoard;
import com.chess.minimax.GameContext;

public class Evaluator {

    public static final int PAWN_VALUE = 100;
    public static final int KNIGHT_VALUE = 320;
    public static final int BISHOP_VALUE = 330;
    public static final int ROOK_VALUE = 500;
    public static final int QUEEN_VALUE = 900;
    public static final int KING_VALUE = 20000;

    // Piece-Square Tables (PSQT) based on Stockfish-inspired values
    public static final int[][] PAWN_MG = {
        {0, 0, 0, 0, 0, 0, 0, 0},
        {3, 3, 10, 19, 16, 19, 7, -5},
        {-9, -15, 11, 15, 32, 22, 5, -22},
        {-8, -23, 6, 20, 40, 17, 4, -12},
        {13, 0, -13, 1, 11, -2, -13, 5},
        {-5, -12, -7, 22, -8, -5, -15, -18},
        {-7, 7, -3, -13, 5, -16, 10, -8},
        {0, 0, 0, 0, 0, 0, 0, 0}
    };

    public static final int[][] PAWN_EG = {
        {-10, -6, 10, 0, 14, 7, -5, -19},
        {-10, -10, -10, 4, 4, 3, -6, -4},
        {6, -2, -8, -4, -13, -12, -10, -9},
        {9, 4, 3, -12, -12, -6, 13, 8},
        {28, 20, 21, 28, 30, 7, 6, 13},
        {0, -11, 12, 21, 25, 19, 4, 7},
        {0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0}
    };

    public static final int[][] KNIGHT_MG = {
        {-175, -92, -74, -73, -73, -74, -92, -175},
        {-77, -41, -27, -15, -15, -27, -41, -77},
        {-61, -17, 6, 12, 12, 6, -17, -61},
        {-35, 8, 40, 49, 49, 40, 8, -35},
        {-34, 13, 44, 51, 51, 44, 13, -34},
        {-9, 22, 58, 53, 53, 58, 22, -9},
        {-67, -27, 4, 37, 37, 4, -27, -67},
        {-201, -83, -56, -26, -26, -56, -83, -201}
    };

    public static final int[][] KNIGHT_EG = {
        {-96, -65, -49, -21, -21, -49, -65, -96},
        {-67, -54, -18, 8, 8, -18, -54, -67},
        {-40, -27, -8, 29, 29, -8, -27, -40},
        {-35, -2, 13, 28, 28, 13, -2, -35},
        {-45, -16, 9, 39, 39, 9, -16, -45},
        {-51, -44, -16, 17, 17, -16, -44, -51},
        {-69, -50, -51, 12, 12, -51, -50, -69},
        {-100, -88, -56, -17, -17, -56, -88, -100}
    };

    public static final int[][] BISHOP_MG = {
        {-53, -5, -8, -23, -23, -8, -5, -53},
        {-15, 8, 19, 4, 4, 19, 8, -15},
        {-7, 21, -5, 17, 17, -5, 21, -7},
        {-5, 11, 25, 39, 39, 25, 11, -5},
        {-12, 29, 22, 31, 31, 22, 29, -12},
        {-16, 6, 1, 11, 11, 1, 6, -16},
        {-17, -14, 5, 0, 0, 5, -14, -17},
        {-48, 1, -14, -23, -23, -14, 1, -48}
    };

    public static final int[][] BISHOP_EG = {
        {-57, -30, -37, -12, -12, -37, -30, -57},
        {-37, -13, -17, 1, 1, -17, -13, -37},
        {-16, -1, -2, 10, 10, -2, -1, -16},
        {-20, -6, 0, 17, 17, 0, -6, -20},
        {-17, -1, -14, 15, 15, -14, -1, -17},
        {-30, 6, 4, 6, 6, 4, 6, -30},
        {-31, -20, -1, 1, 1, -1, -20, -31},
        {-46, -42, -37, -24, -24, -37, -42, -46}
    };

    public static final int[][] ROOK_MG = {
        {-31, -20, -14, -5, -5, -14, -20, -31},
        {-21, -13, -8, 6, 6, -8, -13, -21},
        {-25, -11, -1, 3, 3, -1, -11, -25},
        {-13, -5, -4, -6, -6, -4, -5, -13},
        {-27, -15, -4, 3, 3, -4, -15, -27},
        {-22, -2, 6, 12, 12, 6, -2, -22},
        {-2, 12, 16, 18, 18, 16, 12, -2},
        {-17, -19, -1, 9, 9, -1, -19, -17}
    };

    public static final int[][] ROOK_EG = {
        {-9, -13, -10, -9, -9, -10, -13, -9},
        {-12, -9, -1, -2, -2, -1, -9, -12},
        {6, -8, -2, -6, -6, -2, -8, 6},
        {-6, 1, -9, 7, 7, -9, 1, -6},
        {-5, 8, 7, -6, -6, 7, 8, -5},
        {6, 1, -7, 10, 10, -7, 1, 6},
        {4, 5, 20, -5, -5, 20, 5, 4},
        {18, 0, 19, 13, 13, 19, 0, 18}
    };

    public static final int[][] KING_MG = {
        {271, 327, 271, 198, 198, 271, 327, 271},
        {278, 303, 234, 179, 179, 234, 303, 278},
        {195, 258, 169, 120, 120, 169, 258, 195},
        {164, 190, 138, 98, 98, 138, 190, 164},
        {154, 179, 105, 70, 70, 105, 179, 154},
        {123, 145, 81, 31, 31, 81, 145, 123},
        {88, 120, 65, 33, 33, 65, 120, 88},
        {59, 89, 45, -1, -1, 45, 89, 59}
    };

    public static final int[][] KING_EG = {
        {1, 45, 85, 76, 76, 85, 45, 1},
        {53, 100, 133, 135, 135, 133, 100, 53},
        {88, 130, 169, 175, 175, 169, 130, 88},
        {103, 156, 172, 172, 172, 172, 156, 103},
        {96, 166, 199, 199, 199, 199, 166, 96},
        {92, 172, 184, 191, 191, 184, 172, 92},
        {47, 121, 116, 131, 131, 116, 121, 47},
        {11, 59, 73, 78, 78, 73, 59, 11}
    };

    /**
     * Evaluates the current chess board state by combining material values,
     * positional bonuses, game phase (midgame vs endgame), and dynamic factors
     * like center control, king safety, and mobility. Adjusts the score based
     * on which player's turn it is to move.
     *
     * @param board The chess board to evaluate.
     * @return An integer score indicating the evaluation of the position
     * (positive values favor White, negative values favor Black).
     */
    public int evaluate(ChessBoard board) {
        int[][] b = board.getBoard();
        int mgScore = 0;
        int egScore = 0;
        int phase = 0;

        int whiteKingRow = -1, whiteKingCol = -1;
        int blackKingRow = -1, blackKingCol = -1;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = b[row][col];
                if (piece == 0) {
                    continue;
                }

                boolean isWhite = piece > 0;
                int abs = Math.abs(piece);
                int valMG = 0, valEG = 0;

                switch (abs) {
                    case 1 -> {
                        valMG = PAWN_VALUE + (isWhite ? PAWN_MG[row][col] : -PAWN_MG[7 - row][col]);
                        valEG = PAWN_VALUE + (isWhite ? PAWN_EG[row][col] : -PAWN_EG[7 - row][col]);
                    }
                    case 2 -> {
                        valMG = ROOK_VALUE + (isWhite ? ROOK_MG[row][col] : -ROOK_MG[7 - row][col]);
                        valEG = ROOK_VALUE + (isWhite ? ROOK_EG[row][col] : -ROOK_EG[7 - row][col]);
                        phase += 2;
                    }
                    case 3 -> {
                        valMG = KNIGHT_VALUE + (isWhite ? KNIGHT_MG[row][col] : -KNIGHT_MG[7 - row][col]);
                        valEG = KNIGHT_VALUE + (isWhite ? KNIGHT_EG[row][col] : -KNIGHT_EG[7 - row][col]);
                        phase += 1;

                        // Penalize knight on rim in early game
                        if (GameContext.getPly() < 4 && (col == 0 || col == 7 || row == 0 || row == 7)) {
                            valMG -= 100;
                            valEG -= 80;
                        }

                        // Bonus for central knight development (e.g. Nf3/Nc3)
                        if (GameContext.getPly() < 6 && isWhite && row == 5 && (col == 2 || col == 5)) {
                            valMG += 40;
                            valEG += 20;
                        }
                    }
                    case 4 -> {
                        valMG = BISHOP_VALUE + (isWhite ? BISHOP_MG[row][col] : -BISHOP_MG[7 - row][col]);
                        valEG = BISHOP_VALUE + (isWhite ? BISHOP_EG[row][col] : -BISHOP_EG[7 - row][col]);
                        phase += 1;
                    }
                    case 5 -> {
                        valMG = QUEEN_VALUE;
                        valEG = QUEEN_VALUE;
                        phase += 4;
                    }
                    case 6 -> {
                        valMG = isWhite ? KING_MG[row][col] : -KING_MG[7 - row][col];
                        valEG = isWhite ? KING_EG[row][col] : -KING_EG[7 - row][col];
                        if (isWhite) {
                            whiteKingRow = row;
                            whiteKingCol = col;
                        } else {
                            blackKingRow = row;
                            blackKingCol = col;
                        }
                    }
                }

                mgScore += isWhite ? valMG : -valMG;
                egScore += isWhite ? valEG : -valEG;
            }
        }

        int finalScore = (mgScore * phase + egScore * (24 - phase)) / 24;

        // Tempo bonus
        finalScore += GameContext.isWhiteToMove() ? 15 : -15;

        // Basic mobility bonus
        finalScore += board.getAllLegalMoves(GameContext.getCurrentPlayer()).size();

        // Extra center pawn push bonus in opening
        if (GameContext.getPly() == 0) {
            if (b[6][4] == 1) {
                finalScore += 100; // e2 still available
            }
            if (b[6][3] == 1) {
                finalScore += 100; // d2 still available
            }
            if (b[4][4] == 1) {
                finalScore += 100; // pawn on e4
            }
            if (b[4][3] == 1) {
                finalScore += 100; // pawn on d4
            }
            // Penalize edge pawns and quiet non-developing pawn moves
            if (b[5][0] == 1 || b[5][7] == 1 || b[5][2] == 1 || b[5][5] == 1) {
                finalScore -= 40;
            }

            // Penalize lack of center pawn play
            boolean centerFree = b[4][4] != 1 && b[4][3] != 1;
            if (centerFree) {
                finalScore -= 50;
            }
        }

        // Center control bonus
        for (int row = 3; row <= 4; row++) {
            for (int col = 3; col <= 4; col++) {
                int piece = b[row][col];
                if (piece == 1) {
                    finalScore += 20;
                }
                if (piece == -1) {
                    finalScore -= 20;
                }
            }
        }

        // King safety (pawn shield)
        if (whiteKingRow != -1 && countPawnShield(board, whiteKingRow, whiteKingCol, true) < 2) {
            finalScore -= 40;
        }
        if (blackKingRow != -1 && countPawnShield(board, blackKingRow, blackKingCol, false) < 2) {
            finalScore += 40;
        }

        // Normalize score
        if (GameContext.isBlackToMove()) {
            finalScore = -finalScore;
        }

        return finalScore;
    }

    /**
     * Counts the number of friendly pawns protecting the king by checking the
     * three squares directly in front of the king based on the player's color.
     * This is used as part of the king safety evaluation in board scoring.
     *
     * @param board The chess board to inspect.
     * @param kingRow The row position of the king.
     * @param kingCol The column position of the king.
     * @param isWhite True if checking for the White king's pawn shield; false
     * if for the Black king.
     * @return The number of pawns found protecting the king.
     */
    private int countPawnShield(ChessBoard board, int kingRow, int kingCol, boolean isWhite) {
        int[][] b = board.getBoard();
        int count = 0;
        int dir = isWhite ? 1 : -1;

        for (int dc = -1; dc <= 1; dc++) {
            int r = kingRow + dir;
            int c = kingCol + dc;
            if (r >= 0 && r < 8 && c >= 0 && c < 8) {
                int piece = b[r][c];
                if ((isWhite && piece == 1) || (!isWhite && piece == -1)) {
                    count++;
                }
            }
        }
        return count;
    }
}
