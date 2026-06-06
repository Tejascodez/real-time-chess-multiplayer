package com.tejas.multiplayer_chess.service;

import com.tejas.multiplayer_chess.dto.*;
import com.tejas.multiplayer_chess.engine.ChessEngine;
import com.tejas.multiplayer_chess.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final RoomService roomService;

    public MoveMadeResponse processMove(MakeMoveRequest request) {

        GameRoom room = roomService.getRoom(request.getRoomId());

        if (room.getRoomStatus() != RoomStatus.IN_PROGRESS) {
            log.warn("Move rejected — room {} not IN_PROGRESS", request.getRoomId());
            return null;
        }

        if (!isPlayersTurn(room, request.getPlayerId())) {
            log.warn("Move rejected — not player {}'s turn", request.getPlayerId());
            return null;
        }

        ChessEngine engine = new ChessEngine(room.getCurrentFen());

        long now = System.currentTimeMillis();
        long elapsed = now - room.getLastMoveTimestamp();
        deductTime(room, engine.getCurrentTurn(), elapsed);
        room.setLastMoveTimestamp(now);

        ChessMove move = ChessMove.builder()
            .from(request.getFrom())
            .to(request.getTo())
            .promotion(request.getPromotion())
            .build();

        boolean legal = engine.makeMove(move);

        if (!legal) {
            log.info("Illegal move: {} → {} in room {}",
                request.getFrom(), request.getTo(), request.getRoomId());
            return null;
        }

        String newFen = engine.getCurrentFen();
        room.setCurrentFen(newFen);
        room.getMoveHistory().add(request.getFrom() + request.getTo() +
            (request.getPromotion() != null ? request.getPromotion() : ""));

        GameStatus gameStatus = engine.getGameStatus();

        if (gameStatus != GameStatus.ONGOING) {
            roomService.finishRoom(request.getRoomId());
        }

        roomService.saveRoom(room);

        return MoveMadeResponse.builder()
            .from(request.getFrom())
            .to(request.getTo())
            .promotion(request.getPromotion())
            .newFen(newFen)
            .nextTurn(engine.getCurrentTurn())
            .inCheck(engine.isInCheck())
            .gameStatus(gameStatus)
            .whiteTimeRemainingMs(room.getWhiteTimeRemainingMs())
            .blackTimeRemainingMs(room.getBlackTimeRemainingMs())
            .build();
    }

    public GameOverResponse processResign(String roomId, String playerId) {

        GameRoom room = roomService.getRoom(roomId);
        Player player = getPlayer(room, playerId);

        GameStatus result = player.getColor() == PlayerColor.WHITE
            ? GameStatus.BLACK_WINS
            : GameStatus.WHITE_WINS;

        Player winner = player.getColor() == PlayerColor.WHITE
            ? room.getBlackPlayer()
            : room.getWhitePlayer();

        roomService.finishRoom(roomId);

        return GameOverResponse.builder()
            .gameStatus(result)
            .reason("RESIGN")
            .winnerPlayerId(winner != null ? winner.getId() : null)
            .build();
    }

    public GameOverResponse processTimeout(String roomId, PlayerColor timedOutColor) {

        roomService.finishRoom(roomId);

        GameStatus result = timedOutColor == PlayerColor.WHITE
            ? GameStatus.BLACK_WINS
            : GameStatus.WHITE_WINS;

        GameRoom room = roomService.getRoom(roomId);
        Player winner = timedOutColor == PlayerColor.WHITE
            ? room.getBlackPlayer()
            : room.getWhitePlayer();

        return GameOverResponse.builder()
            .gameStatus(result)
            .reason("TIMEOUT")
            .winnerPlayerId(winner != null ? winner.getId() : null)
            .build();
    }

    public GameOverResponse buildGameOverResponse(String roomId, GameStatus status) {

        GameRoom room = roomService.getRoom(roomId);

        String winnerId = null;
        if (status == GameStatus.WHITE_WINS)
            winnerId = room.getWhitePlayer() != null ? room.getWhitePlayer().getId() : null;
        else if (status == GameStatus.BLACK_WINS)
            winnerId = room.getBlackPlayer() != null ? room.getBlackPlayer().getId() : null;

        String reason = switch (status) {
            case WHITE_WINS, BLACK_WINS -> "CHECKMATE";
            case STALEMATE              -> "STALEMATE";
            case DRAW_FIFTY_MOVE        -> "FIFTY_MOVE_RULE";
            default                     -> status.name();
        };

        return GameOverResponse.builder()
            .gameStatus(status)
            .reason(reason)
            .winnerPlayerId(winnerId)
            .build();
    }

    private boolean isPlayersTurn(GameRoom room, String playerId) {
        ChessEngine engine = new ChessEngine(room.getCurrentFen());
        PlayerColor turn = engine.getCurrentTurn();

        Player white = room.getWhitePlayer();
        Player black = room.getBlackPlayer();

        if (turn == PlayerColor.WHITE) {
            return white != null && white.getId().equals(playerId);
        } else {
            return black != null && black.getId().equals(playerId);
        }
    }

    private void deductTime(GameRoom room, PlayerColor color, long ms) {
        if (color == PlayerColor.WHITE) {
            room.setWhiteTimeRemainingMs(
                Math.max(0, room.getWhiteTimeRemainingMs() - ms)
            );
        } else {
            room.setBlackTimeRemainingMs(
                Math.max(0, room.getBlackTimeRemainingMs() - ms)
            );
        }
    }

    private Player getPlayer(GameRoom room, String playerId) {
        if (room.getWhitePlayer() != null &&
            room.getWhitePlayer().getId().equals(playerId)) {
            return room.getWhitePlayer();
        }
        if (room.getBlackPlayer() != null &&
            room.getBlackPlayer().getId().equals(playerId)) {
            return room.getBlackPlayer();
        }
        throw new IllegalArgumentException("Player not in room");
    }
}