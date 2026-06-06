package com.tejas.multiplayer_chess.engine;

import com.tejas.multiplayer_chess.engine.pieces.*;
import com.tejas.multiplayer_chess.model.ChessMove;
import com.tejas.multiplayer_chess.model.GameStatus;
import com.tejas.multiplayer_chess.model.PlayerColor;

import java.util.ArrayDeque;
import java.util.Deque;

public class ChessEngine {

    private Board      board;
    private GameState  state;

    private final MoveValidator  moveValidator;
    private final CheckDetector  checkDetector;
    private final FenParser      fenParser;
    private final FenGenerator   fenGenerator;

    private final Deque<String> history = new ArrayDeque<>();

    private static final String STARTING_FEN =
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public ChessEngine() {
        this(STARTING_FEN);
    }

    public ChessEngine(String fen) {
        this.fenParser     = new FenParser();
        this.fenGenerator  = new FenGenerator();
        this.moveValidator = new MoveValidator();
        this.checkDetector = new CheckDetector(moveValidator);
        this.moveValidator.setCheckDetector(checkDetector);

        FenParser.ParseResult parsed = fenParser.parseFull(fen);
        this.board = parsed.board();
        this.state = parsed.state();
    }

    // ----------------------------------------------------------------
    //  Core: makeMove
    // ----------------------------------------------------------------

    public boolean makeMove(ChessMove move) {

        if (getGameStatus() != GameStatus.ONGOING) return false;

        int sr = 8 - Character.getNumericValue(move.getFrom().charAt(1));
        int sc = move.getFrom().charAt(0) - 'a';
        int er = 8 - Character.getNumericValue(move.getTo().charAt(1));
        int ec = move.getTo().charAt(0) - 'a';

        Box   startBox    = board.getBox(sr, sc);
        Piece movingPiece = startBox.getPiece();

        if (movingPiece == null) return false;
        if (movingPiece.getColor() != state.getActiveColor()) return false;

        boolean isEnPassant = isEnPassantMove(movingPiece, sr, sc, er, ec);
        boolean isCastling  = isCastlingMove(movingPiece, sr, sc, er, ec);

        // --- Validate ---
        if (isEnPassant) {
            if (checkDetector.moveLeavesKingInCheck(board, sr, sc, er, ec,
                    movingPiece.getColor())) {
                return false;
            }
        } else if (isCastling) {
            King king = (King) movingPiece;
            if (!king.isCastlingLegal(sr, sc, er, ec, board, state, checkDetector)) {
                return false;
            }
        } else {
            if (!moveValidator.isValidMove(board, sr, sc, er, ec)) return false;
        }

        // ✅ FIX LINE 1 — capture BEFORE executing the move
        // After execution the destination square holds the moving piece,
        // not the captured piece, so getPiece() != null would always be true
        boolean isCapture = board.getBox(er, ec).getPiece() != null;

        history.push(getCurrentFen());

        // --- Execute move ---
        if (isEnPassant) {
            executeEnPassant(sr, sc, er, ec, movingPiece);
        } else if (isCastling) {
            executeCastling(sr, sc, er, ec, movingPiece);
        } else {
            executeNormal(sr, sc, er, ec, movingPiece, move.getPromotion());
        }

        // ✅ FIX LINE 2 — pass isCapture flag into updateStateAfterMove
        updateStateAfterMove(movingPiece, sr, sc, er, ec, isEnPassant, isCapture);

        return true;
    }

    // ----------------------------------------------------------------
    //  Move execution helpers
    // ----------------------------------------------------------------

    private void executeNormal(
            int sr, int sc, int er, int ec,
            Piece movingPiece, String promotion
    ) {
        Box endBox = board.getBox(er, ec);

        if (movingPiece instanceof Pawn && promotion != null) {
            endBox.setPiece(resolvePromotion(promotion, movingPiece.getColor()));
        } else {
            endBox.setPiece(movingPiece);
        }

        board.getBox(sr, sc).setPiece(null);
    }

    private void executeEnPassant(
            int sr, int sc, int er, int ec,
            Piece movingPiece
    ) {
        board.getBox(er, ec).setPiece(movingPiece);
        board.getBox(sr, sc).setPiece(null);
        board.getBox(sr, ec).setPiece(null); // remove captured pawn
    }

    private void executeCastling(
            int sr, int sc, int er, int ec,
            Piece king
    ) {
        boolean kingSide = ec > sc;

        board.getBox(er, ec).setPiece(king);
        board.getBox(sr, sc).setPiece(null);

        int rookFromCol = kingSide ? 7 : 0;
        int rookToCol   = kingSide ? ec - 1 : ec + 1;

        Piece rook = board.getBox(sr, rookFromCol).getPiece();
        board.getBox(sr, rookToCol).setPiece(rook);
        board.getBox(sr, rookFromCol).setPiece(null);
    }

    // ----------------------------------------------------------------
    //  GameState update after move
    // ----------------------------------------------------------------

    // ✅ FIX LINE 3 — added wasCapture parameter to the signature
    private void updateStateAfterMove(
            Piece movingPiece,
            int sr, int sc,
            int er, int ec,
            boolean wasEnPassant,
            boolean wasCapture      // ← new parameter
    ) {
        PlayerColor color = movingPiece.getColor();

        state.clearEnPassant();

        if (movingPiece instanceof Pawn) {
            int direction = color == PlayerColor.WHITE ? -1 : 1;
            if (Math.abs(er - sr) == 2) {
                state.setEnPassant(sr + direction, sc);
            }
        }

        if (movingPiece instanceof King) {
            state.revokeAllCastling(color);
        }

        if (movingPiece instanceof Rook) {
            if (sc == 0) state.revokeCastling(color, false);
            if (sc == 7) state.revokeCastling(color, true);
        }

        revokeIfRookCaptured(er, ec);

        boolean isPawnMove = movingPiece instanceof Pawn;
        // ✅ now uses the flag captured before execution — always correct
        boolean isCapture  = wasEnPassant || wasCapture;

        if (isPawnMove || isCapture) {
            state.setHalfMoveClock(0);
        } else {
            state.setHalfMoveClock(state.getHalfMoveClock() + 1);
        }

        if (color == PlayerColor.BLACK) {
            state.setFullMoveNumber(state.getFullMoveNumber() + 1);
        }

        state.setActiveColor(CheckDetector.opponent(color));
    }

    private void revokeIfRookCaptured(int er, int ec) {
        if (er == 0 && ec == 0) state.revokeCastling(PlayerColor.BLACK, false);
        if (er == 0 && ec == 7) state.revokeCastling(PlayerColor.BLACK, true);
        if (er == 7 && ec == 0) state.revokeCastling(PlayerColor.WHITE, false);
        if (er == 7 && ec == 7) state.revokeCastling(PlayerColor.WHITE, true);
    }

    // ----------------------------------------------------------------
    //  Special move detection
    // ----------------------------------------------------------------

    private boolean isEnPassantMove(
            Piece piece, int sr, int sc, int er, int ec
    ) {
        return piece instanceof Pawn &&
               ((Pawn) piece).isEnPassantMove(sr, sc, er, ec, state);
    }

    private boolean isCastlingMove(
            Piece piece, int sr, int sc, int er, int ec
    ) {
        return piece instanceof King &&
               ((King) piece).isCastlingMove(sc, ec);
    }

    // ----------------------------------------------------------------
    //  Undo
    // ----------------------------------------------------------------

    public boolean undoMove() {
        if (history.isEmpty()) return false;

        String previousFen = history.pop();
        FenParser.ParseResult restored = fenParser.parseFull(previousFen);
        this.board = restored.board();
        this.state = restored.state();

        return true;
    }

    // ----------------------------------------------------------------
    //  Game status
    // ----------------------------------------------------------------

    public GameStatus getGameStatus() {
        PlayerColor active = state.getActiveColor();

        if (checkDetector.isCheckmate(board, active)) {
            return active == PlayerColor.WHITE
                ? GameStatus.BLACK_WINS
                : GameStatus.WHITE_WINS;
        }

        if (checkDetector.isStalemate(board, active)) {
            return GameStatus.STALEMATE;
        }

        if (state.getHalfMoveClock() >= 100) {
            return GameStatus.DRAW_FIFTY_MOVE;
        }

        return GameStatus.ONGOING;
    }

    public boolean isInCheck() {
        return checkDetector.isInCheck(board, state.getActiveColor());
    }

    // ----------------------------------------------------------------
    //  Public accessors
    // ----------------------------------------------------------------

    public PlayerColor getCurrentTurn()  { return state.getActiveColor(); }
    public String      getCurrentFen()   { return fenGenerator.generate(board, state); }
    public Board       getBoard()        { return board; }
    public GameState   getGameState()    { return state; }
    public boolean     canUndo()         { return !history.isEmpty(); }

    // ----------------------------------------------------------------
    //  Private helpers
    // ----------------------------------------------------------------

    private Piece resolvePromotion(String promotion, PlayerColor color) {
        return switch (promotion.toLowerCase()) {
            case "q" -> new Queen(color);
            case "r" -> new Rook(color);
            case "b" -> new Bishop(color);
            case "n" -> new Knight(color);
            default  -> new Queen(color);
        };
    }
}