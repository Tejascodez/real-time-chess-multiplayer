package com.tejas.multiplayer_chess.engine;

import com.tejas.multiplayer_chess.engine.pieces.*;
import com.tejas.multiplayer_chess.model.PlayerColor;

/**
 * Generates a complete, valid FEN string from Board + GameState.
 * The output can be fed back into FenParser to perfectly restore state.
 */
public class FenGenerator {

    public String generate(Board board, GameState state) {

        return String.join(" ",
            generateBoard(board),       // field 1: piece placement
            generateActiveColor(state), // field 2: active color
            generateCastling(state),    // field 3: castling rights
            generateEnPassant(state),   // field 4: en passant square
            String.valueOf(state.getHalfMoveClock()),  // field 5
            String.valueOf(state.getFullMoveNumber())  // field 6
        );
    }

    // ----------------------------------------------------------------
    //  Field generators
    // ----------------------------------------------------------------

    private String generateBoard(Board board) {

        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < 8; row++) {

            int empty = 0;

            for (int col = 0; col < 8; col++) {

                Piece piece = board.getBox(row, col).getPiece();

                if (piece == null) {
                    empty++;
                } else {
                    if (empty > 0) {
                        sb.append(empty);
                        empty = 0;
                    }
                    sb.append(toFenChar(piece));
                }
            }

            if (empty > 0) sb.append(empty);
            if (row != 7)  sb.append('/');
        }

        return sb.toString();
    }

    private String generateActiveColor(GameState state) {
        return state.getActiveColor() == PlayerColor.WHITE ? "w" : "b";
    }

    private String generateCastling(GameState state) {

        StringBuilder sb = new StringBuilder();

        if (state.isWhiteKingSide())  sb.append('K');
        if (state.isWhiteQueenSide()) sb.append('Q');
        if (state.isBlackKingSide())  sb.append('k');
        if (state.isBlackQueenSide()) sb.append('q');

        return sb.isEmpty() ? "-" : sb.toString();
    }

    private String generateEnPassant(GameState state) {

        if (!state.hasEnPassant()) return "-";

        // row → rank digit:  row 5 → rank '3'
        char file = (char) ('a' + state.getEnPassantCol());
        char rank = (char) ('0' + (8 - state.getEnPassantRow()));

        return String.valueOf(file) + rank;
    }

    // ----------------------------------------------------------------
    //  Piece → FEN character
    // ----------------------------------------------------------------

    private char toFenChar(Piece piece) {

        char ch = switch (piece) {
            case King   k -> 'k';
            case Queen  q -> 'q';
            case Rook   r -> 'r';
            case Bishop b -> 'b';
            case Knight n -> 'n';
            default       -> 'p';   // Pawn
        };

        return piece.getColor() == PlayerColor.WHITE
            ? Character.toUpperCase(ch)
            : ch;
    }
}