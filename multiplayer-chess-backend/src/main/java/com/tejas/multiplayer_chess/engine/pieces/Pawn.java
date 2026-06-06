package com.tejas.multiplayer_chess.engine.pieces;

import com.tejas.multiplayer_chess.engine.Board;
import com.tejas.multiplayer_chess.engine.Box;
import com.tejas.multiplayer_chess.engine.GameState;
import com.tejas.multiplayer_chess.model.PlayerColor;

public class Pawn extends Piece {

    public Pawn(PlayerColor color) {
        super(color);
    }

    /**
     * Standard move validation (no en passant — GameState not available here).
     * En passant is handled separately in ChessEngine before this is called.
     */
    @Override
    public boolean isValidMove(
            int sr, int sc,
            int er, int ec,
            Board board
    ) {
        int direction = color == PlayerColor.WHITE ? -1 : 1;

        // --- Forward moves (same column) ---
        if (sc == ec) {

            // One step forward — destination must be empty
            if (er == sr + direction) {
                return board.getBox(er, ec).getPiece() == null;
            }

            // Two steps from starting rank
            boolean onStartRank =
                (color == PlayerColor.WHITE && sr == 6) ||
                (color == PlayerColor.BLACK && sr == 1);

            if (onStartRank && er == sr + 2 * direction) {
                // BOTH the intermediate square AND the destination must be empty
                int middleRow = sr + direction;
                return board.getBox(middleRow, sc).getPiece() == null &&
                       board.getBox(er, ec).getPiece() == null;
            }
        }

        // --- Diagonal capture ---
        if (Math.abs(sc - ec) == 1 && er == sr + direction) {
            Box target = board.getBox(er, ec);
            return target.getPiece() != null &&
                   target.getPiece().getColor() != color;
        }

        return false;
    }

    /**
     * En passant check — needs GameState for the target square.
     * Called from ChessEngine separately.
     */
    public boolean isEnPassantMove(
            int sr, int sc,
            int er, int ec,
            GameState state
    ) {
        if (!state.hasEnPassant()) return false;

        int direction = color == PlayerColor.WHITE ? -1 : 1;

        return er == sr + direction &&
               Math.abs(sc - ec) == 1 &&
               er == state.getEnPassantRow() &&
               ec == state.getEnPassantCol();
    }
}