package com.tejas.multiplayer_chess.util;

import com.tejas.multiplayer_chess.engine.Board;
import com.tejas.multiplayer_chess.engine.Box;
import com.tejas.multiplayer_chess.engine.pieces.King;
import com.tejas.multiplayer_chess.engine.pieces.Piece;
import com.tejas.multiplayer_chess.model.PlayerColor;

public class BoardUtil {

    /**
     * Returns true if every square between start and end is empty.
     * Works for diagonals, ranks, and files.
     * Does NOT check the start or end squares themselves.
     */
    public static boolean isPathClear(
            Board board,
            int sr, int sc,
            int er, int ec
    ) {
        int rowStep = Integer.compare(er, sr);
        int colStep = Integer.compare(ec, sc);

        int row = sr + rowStep;
        int col = sc + colStep;

        while (row != er || col != ec) {
            if (board.getBox(row, col).getPiece() != null) {
                return false;
            }
            row += rowStep;
            col += colStep;
        }

        return true;
    }

    /**
     * Finds the king of the given color on the board.
     * Returns int[]{row, col}, or null if somehow not found
     * (should never happen in a valid game).
     */
    public static int[] findKing(Board board, PlayerColor color) {

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getBox(row, col).getPiece();
                if (piece instanceof King &&
                    piece.getColor() == color) {
                    return new int[]{row, col};
                }
            }
        }

        return null; // Should never happen in a valid game
    }
}