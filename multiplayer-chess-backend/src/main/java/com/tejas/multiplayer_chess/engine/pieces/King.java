package com.tejas.multiplayer_chess.engine.pieces;

import com.tejas.multiplayer_chess.engine.Board;
import com.tejas.multiplayer_chess.engine.GameState;
import com.tejas.multiplayer_chess.model.PlayerColor;

public class King extends Piece {

    public King(PlayerColor color) {
        super(color);
    }

    /**
     * Standard one-square move.
     * Castling is validated separately in ChessEngine
     * because it needs GameState and CheckDetector.
     */
    @Override
    public boolean isValidMove(
            int sr, int sc,
            int er, int ec,
            Board board
    ) {
        return Math.abs(sr - er) <= 1 &&
               Math.abs(sc - ec) <= 1;
    }

    /**
     * Returns true if this is a castling attempt
     * (king moves exactly 2 squares sideways).
     */
    public boolean isCastlingMove(int sc, int ec) {
        return Math.abs(sc - ec) == 2;
    }

    /**
     * Full castling legality check.
     *
     * Rules:
     *  1. King has castling rights (hasn't moved)
     *  2. Rook has castling rights (hasn't moved)
     *  3. Squares between king and rook are empty
     *  4. King is not currently in check
     *  5. King does not pass through a square under attack
     *  6. King does not end up in check
     */
    public boolean isCastlingLegal(
            int sr, int sc,
            int er, int ec,
            Board board,
            GameState state,
            com.tejas.multiplayer_chess.engine.CheckDetector checkDetector
    ) {
        boolean kingSide = ec > sc; // King moves right → king-side

        // 1. Castling rights
        if (!state.canCastle(color, kingSide)) return false;

        // 2. Path must be clear between king and rook
        int rookCol  = kingSide ? 7 : 0;
        int stepCol  = kingSide ? 1 : -1;

        for (int col = sc + stepCol; col != rookCol; col += stepCol) {
            if (board.getBox(sr, col).getPiece() != null) return false;
        }

        // 3. King must not be in check right now
        if (checkDetector.isInCheck(board, color)) return false;

        // 4. King must not pass through an attacked square
        int passThroughCol = sc + stepCol;

        Board passBoard = board.deepCopy();
        passBoard.getBox(sr, passThroughCol).setPiece(this);
        passBoard.getBox(sr, sc).setPiece(null);

        if (checkDetector.isInCheck(passBoard, color)) return false;

        // 5. King must not land on an attacked square (checked by caller
        //    via moveLeavesKingInCheck, but we also verify here)
        Board landBoard = board.deepCopy();
        landBoard.getBox(sr, ec).setPiece(this);
        landBoard.getBox(sr, sc).setPiece(null);

        return !checkDetector.isInCheck(landBoard, color);
    }
}