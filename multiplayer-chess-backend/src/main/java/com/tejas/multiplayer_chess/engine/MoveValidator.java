package com.tejas.multiplayer_chess.engine;

import com.tejas.multiplayer_chess.engine.pieces.*;
import com.tejas.multiplayer_chess.model.PlayerColor;

public class MoveValidator {

    private CheckDetector checkDetector;

    // Injected after construction to avoid circular dependency
    public void setCheckDetector(CheckDetector checkDetector) {
        this.checkDetector = checkDetector;
    }

    public boolean isValidMove(
            Board board,
            int sr, int sc,
            int er, int ec
    ) {
        if (!isInsideBoard(sr, sc) || !isInsideBoard(er, ec)) return false;
        if (sr == er && sc == ec)                              return false;

        Box startBox = board.getBox(sr, sc);
        Piece piece  = startBox.getPiece();

        if (piece == null) return false;

        Box targetBox = board.getBox(er, ec);

        // Cannot capture own piece
        if (targetBox.getPiece() != null &&
            targetBox.getPiece().getColor() == piece.getColor()) {
            return false;
        }

        // Piece-specific movement
        if (!piece.isValidMove(sr, sc, er, ec, board)) return false;

        // Post-move check guard — skip during CheckDetector's
        // own simulation to avoid infinite recursion
        if (checkDetector != null) {
            if (checkDetector.moveLeavesKingInCheck(
                    board, sr, sc, er, ec, piece.getColor())) {
                return false;
            }
        }

        return true;
    }

    private boolean isInsideBoard(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}