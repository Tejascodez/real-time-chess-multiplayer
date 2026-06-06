package com.tejas.multiplayer_chess.engine;

import com.tejas.multiplayer_chess.engine.pieces.Piece;
import com.tejas.multiplayer_chess.model.PlayerColor;
import com.tejas.multiplayer_chess.util.BoardUtil;

/**
 * Detects check, checkmate, and stalemate.
 *
 * Key design principle: ALL methods work on a Board snapshot.
 * They never touch real game state — callers pass in a board
 * (possibly a deep copy for simulation) and a color to test.
 */
public class CheckDetector {

    private final MoveValidator moveValidator;

    public CheckDetector(MoveValidator moveValidator) {
        this.moveValidator = moveValidator;
    }

    // ----------------------------------------------------------------
    //  Core: is this color's king currently in check?
    // ----------------------------------------------------------------

    /**
     * Returns true if 'color' king is attacked by any enemy piece.
     */
    public boolean isInCheck(Board board, PlayerColor color) {

        int[] kingPos = BoardUtil.findKing(board, color);

        if (kingPos == null) return false; // Shouldn't happen

        int kingRow = kingPos[0];
        int kingCol = kingPos[1];

        PlayerColor enemy = opponent(color);

        // Scan every square — if an enemy piece can legally move
        // to the king's square, the king is in check
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Piece piece = board.getBox(row, col).getPiece();

                if (piece == null || piece.getColor() != enemy) continue;

                // Use the raw piece movement check, NOT moveValidator,
                // to avoid circular calls through check validation
                if (piece.isValidMove(row, col, kingRow, kingCol, board)) {
                    return true;
                }
            }
        }

        return false;
    }

    // ----------------------------------------------------------------
    //  Does a specific move leave the mover's king in check?
    // ----------------------------------------------------------------

    /**
     * Simulates the move on a board copy and tests for self-check.
     * Returns true if the move is ILLEGAL because it exposes own king.
     */
    public boolean moveLeavesKingInCheck(
            Board board,
            int sr, int sc,
            int er, int ec,
            PlayerColor color
    ) {
        // Work on a copy — never mutate the real board
        Board copy = board.deepCopy();

        Piece moving = copy.getBox(sr, sc).getPiece();

        copy.getBox(er, ec).setPiece(moving);
        copy.getBox(sr, sc).setPiece(null);

        return isInCheck(copy, color);
    }

    // ----------------------------------------------------------------
    //  Checkmate and stalemate
    // ----------------------------------------------------------------

    /**
     * Checkmate: color is in check AND has no legal moves.
     */
    public boolean isCheckmate(Board board, PlayerColor color) {
        return isInCheck(board, color) && hasNoLegalMoves(board, color);
    }

    /**
     * Stalemate: color is NOT in check but has no legal moves.
     */
    public boolean isStalemate(Board board, PlayerColor color) {
        return !isInCheck(board, color) && hasNoLegalMoves(board, color);
    }

    // ----------------------------------------------------------------
    //  Legal move enumeration
    // ----------------------------------------------------------------

    /**
     * Returns true if the given color has zero legal moves.
     * A legal move = passes MoveValidator AND doesn't leave own king in check.
     */
    private boolean hasNoLegalMoves(Board board, PlayerColor color) {

        for (int sr = 0; sr < 8; sr++) {
            for (int sc = 0; sc < 8; sc++) {

                Piece piece = board.getBox(sr, sc).getPiece();

                if (piece == null || piece.getColor() != color) continue;

                // Try every possible destination
                for (int er = 0; er < 8; er++) {
                    for (int ec = 0; ec < 8; ec++) {

                        if (moveValidator.isValidMove(board, sr, sc, er, ec) &&
                            !moveLeavesKingInCheck(board, sr, sc, er, ec, color)) {
                            return false; // Found at least one legal move
                        }
                    }
                }
            }
        }

        return true; // No legal move found
    }

    // ----------------------------------------------------------------
    //  Utility
    // ----------------------------------------------------------------

    public static PlayerColor opponent(PlayerColor color) {
        return color == PlayerColor.WHITE ? PlayerColor.BLACK : PlayerColor.WHITE;
    }
}