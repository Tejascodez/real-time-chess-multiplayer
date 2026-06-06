package com.tejas.multiplayer_chess.engine;

import com.tejas.multiplayer_chess.model.PlayerColor;
import lombok.Data;

/**
 * Holds all game state that lives outside the board grid.
 * Castling rights, en passant target, clocks, and whose turn it is.
 *
 * FEN field mapping:
 *  "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
 *                                                ^  ^^^^  ^ ^ ^
 *                                                |  |     | | fullMoveNumber
 *                                                |  |     | halfMoveClock
 *                                                |  |     enPassantTarget ("-" or "e3")
 *                                                |  castlingRights
 *                                                activeColor
 */
@Data
public class GameState {

    // --- Turn ---
    private PlayerColor activeColor;

    // --- Castling rights ---
    // Each flag means that side still has the RIGHT to castle
    // (king and rook haven't moved). It does NOT mean castling
    // is currently a legal move.
    private boolean whiteKingSide;   // 'K' in FEN
    private boolean whiteQueenSide;  // 'Q' in FEN
    private boolean blackKingSide;   // 'k' in FEN
    private boolean blackQueenSide;  // 'q' in FEN

    // --- En passant ---
    // The square a pawn can move TO when capturing en passant.
    // "-1" means no en passant is available this turn.
    // Example: after White plays e2→e4, enPassantRow=2, enPassantCol=4
    //          (the square "behind" the pawn that just double-moved)
    private int enPassantRow;  // -1 if none
    private int enPassantCol;  // -1 if none

    // --- Clocks ---
    // Half-move clock: moves since last capture or pawn advance.
    // Used for the 50-move draw rule.
    private int halfMoveClock;

    // Full-move number: starts at 1, increments after Black moves.
    private int fullMoveNumber;

    /**
     * Default: standard starting position state.
     */
    public GameState() {
        this.activeColor    = PlayerColor.WHITE;
        this.whiteKingSide  = true;
        this.whiteQueenSide = true;
        this.blackKingSide  = true;
        this.blackQueenSide = true;
        this.enPassantRow   = -1;
        this.enPassantCol   = -1;
        this.halfMoveClock  = 0;
        this.fullMoveNumber = 1;
    }

    /**
     * Deep-copy constructor — used when we simulate a move
     * to test for check without mutating actual game state.
     */
    public GameState(GameState other) {
        this.activeColor    = other.activeColor;
        this.whiteKingSide  = other.whiteKingSide;
        this.whiteQueenSide = other.whiteQueenSide;
        this.blackKingSide  = other.blackKingSide;
        this.blackQueenSide = other.blackQueenSide;
        this.enPassantRow   = other.enPassantRow;
        this.enPassantCol   = other.enPassantCol;
        this.halfMoveClock  = other.halfMoveClock;
        this.fullMoveNumber = other.fullMoveNumber;
    }

    // --- Castling rights helpers ---

    public boolean canCastle(PlayerColor color, boolean kingSide) {
        if (color == PlayerColor.WHITE) {
            return kingSide ? whiteKingSide : whiteQueenSide;
        } else {
            return kingSide ? blackKingSide : blackQueenSide;
        }
    }

    public void revokeCastling(PlayerColor color, boolean kingSide) {
        if (color == PlayerColor.WHITE) {
            if (kingSide) whiteKingSide  = false;
            else          whiteQueenSide = false;
        } else {
            if (kingSide) blackKingSide  = false;
            else          blackQueenSide = false;
        }
    }

    public void revokeAllCastling(PlayerColor color) {
        if (color == PlayerColor.WHITE) {
            whiteKingSide  = false;
            whiteQueenSide = false;
        } else {
            blackKingSide  = false;
            blackQueenSide = false;
        }
    }

    // --- En passant helpers ---

    public boolean hasEnPassant() {
        return enPassantRow != -1;
    }

    public void clearEnPassant() {
        enPassantRow = -1;
        enPassantCol = -1;
    }

    public void setEnPassant(int row, int col) {
        this.enPassantRow = row;
        this.enPassantCol = col;
    }
}