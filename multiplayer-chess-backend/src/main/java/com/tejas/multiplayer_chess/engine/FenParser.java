package com.tejas.multiplayer_chess.engine;

import com.tejas.multiplayer_chess.engine.pieces.*;
import com.tejas.multiplayer_chess.model.PlayerColor;

/**
 * Parses a full FEN string into a Board + GameState pair.
 *
 * FEN format (6 space-separated fields):
 *   <board> <activeColor> <castling> <enPassant> <halfMove> <fullMove>
 *
 * Example:
 *   rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
 */
public class FenParser {

    /**
     * Parse FEN → Board.
     * GameState is discarded — use parseFull() when you need it.
     */
    public Board parse(String fen) {
        return parseFull(fen).board();
    }

    /**
     * Parse FEN → both Board and GameState together.
     */
    public ParseResult parseFull(String fen) {

        String[] parts = fen.trim().split("\\s+");

        // Field 1: piece placement
        Board board = parseBoard(parts[0]);

        GameState state = new GameState();

        // Field 2: active color  ("w" or "b")
        if (parts.length > 1) {
            state.setActiveColor(
                parts[1].equals("w") ? PlayerColor.WHITE : PlayerColor.BLACK
            );
        }

        // Field 3: castling availability ("KQkq", "KQ", "-", etc.)
        if (parts.length > 2) {
            parseCastling(parts[2], state);
        }

        // Field 4: en passant target square ("-" or "e3")
        if (parts.length > 3) {
            parseEnPassant(parts[3], state);
        }

        // Field 5: half-move clock
        if (parts.length > 4) {
            state.setHalfMoveClock(
                parseIntSafe(parts[4], 0)
            );
        }

        // Field 6: full-move number
        if (parts.length > 5) {
            state.setFullMoveNumber(
                parseIntSafe(parts[5], 1)
            );
        }

        return new ParseResult(board, state);
    }

    // ----------------------------------------------------------------
    //  Private helpers
    // ----------------------------------------------------------------

    private Board parseBoard(String boardPart) {

        Board board = new Board();
        String[] rows = boardPart.split("/");

        for (int row = 0; row < 8; row++) {
            int col = 0;
            for (char ch : rows[row].toCharArray()) {
                if (Character.isDigit(ch)) {
                    col += ch - '0';          // empty squares
                } else {
                    board.placePiece(row, col, createPiece(ch));
                    col++;
                }
            }
        }

        return board;
    }

    private void parseCastling(String castling, GameState state) {

        // Start with no rights, then grant what the FEN says
        state.setWhiteKingSide(false);
        state.setWhiteQueenSide(false);
        state.setBlackKingSide(false);
        state.setBlackQueenSide(false);

        if (!castling.equals("-")) {
            for (char ch : castling.toCharArray()) {
                switch (ch) {
                    case 'K' -> state.setWhiteKingSide(true);
                    case 'Q' -> state.setWhiteQueenSide(true);
                    case 'k' -> state.setBlackKingSide(true);
                    case 'q' -> state.setBlackQueenSide(true);
                }
            }
        }
    }

    private void parseEnPassant(String ep, GameState state) {

        if (ep.equals("-") || ep.length() < 2) {
            state.clearEnPassant();
            return;
        }

        // "e3" → col=4, row=5  (FEN uses rank from Black's perspective)
        int col = ep.charAt(0) - 'a';        // 'a'=0 … 'h'=7
        int row = 8 - Character.getNumericValue(ep.charAt(1)); // '3'→row 5
        state.setEnPassant(row, col);
    }

    private Piece createPiece(char ch) {

        PlayerColor color =
            Character.isUpperCase(ch) ? PlayerColor.WHITE : PlayerColor.BLACK;

        return switch (Character.toLowerCase(ch)) {
            case 'k' -> new King(color);
            case 'q' -> new Queen(color);
            case 'r' -> new Rook(color);
            case 'b' -> new Bishop(color);
            case 'n' -> new Knight(color);
            case 'p' -> new Pawn(color);
            default  -> null;
        };
    }

    private int parseIntSafe(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    // ----------------------------------------------------------------
    //  Result record — clean way to return two things at once
    // ----------------------------------------------------------------

    public record ParseResult(Board board, GameState state) {}
}