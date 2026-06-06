package com.tejas.multiplayer_chess.service;

import com.tejas.multiplayer_chess.model.*;
import com.tejas.multiplayer_chess.repository.GameRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final GameRoomRepository roomRepository;

    // ----------------------------------------------------------------
    //  Create
    // ----------------------------------------------------------------

    /**
     * Creates a new room with the given player as White.
     * Room sits in WAITING_FOR_PLAYER until a second player joins.
     */
    public GameRoom createRoom(Player player, long timeLimitMs) {

        player.setColor(PlayerColor.WHITE);

        GameRoom room = GameRoom.builder()
            .roomId(UUID.randomUUID().toString())
            .whitePlayer(player)
            .roomStatus(RoomStatus.WAITING_FOR_PLAYER)
            .whiteTimeRemainingMs(timeLimitMs)
            .blackTimeRemainingMs(timeLimitMs)
            .build();

        roomRepository.save(room);

        log.info("Room created: {} by player: {}", room.getRoomId(), player.getId());

        return room;
    }

    // ----------------------------------------------------------------
    //  Join
    // ----------------------------------------------------------------

    /**
     * Second player joins an existing room as Black.
     * Room transitions to IN_PROGRESS.
     * Throws if room is full or doesn't exist.
     */
    public GameRoom joinRoom(String roomId, Player player) {

        GameRoom room = getRoom(roomId);

        if (room.getRoomStatus() != RoomStatus.WAITING_FOR_PLAYER) {
            throw new IllegalStateException(
                "Room " + roomId + " is not open for joining"
            );
        }

        player.setColor(PlayerColor.BLACK);
        room.setBlackPlayer(player);
        room.setRoomStatus(RoomStatus.IN_PROGRESS);
        room.setLastMoveTimestamp(System.currentTimeMillis());

        roomRepository.save(room);

        log.info("Player {} joined room {}", player.getId(), roomId);

        return room;
    }

    // ----------------------------------------------------------------
    //  Reconnect
    // ----------------------------------------------------------------

    /**
     * Updates a player's session ID when they reconnect.
     * The game continues from the saved FEN — no state is lost.
     */
    public GameRoom reconnectPlayer(String roomId, String playerId,
                                    String newSessionId) {
        GameRoom room = getRoom(roomId);

        Player player = findPlayer(room, playerId);
        player.setSessionId(newSessionId);

        roomRepository.save(room);

        log.info("Player {} reconnected to room {}", playerId, roomId);

        return room;
    }

    // ----------------------------------------------------------------
    //  Disconnect / abandon
    // ----------------------------------------------------------------

    public void handleDisconnect(String roomId, String playerId) {

        GameRoom room = getRoom(roomId);

        // If both players are gone, mark abandoned
        boolean otherOnline = isOtherPlayerOnline(room, playerId);

        if (!otherOnline) {
            room.setRoomStatus(RoomStatus.ABANDONED);
            roomRepository.save(room);
            log.info("Room {} marked ABANDONED", roomId);
        }
    }

    // ----------------------------------------------------------------
    //  Finish
    // ----------------------------------------------------------------

    public void finishRoom(String roomId) {
        GameRoom room = getRoom(roomId);
        room.setRoomStatus(RoomStatus.FINISHED);
        roomRepository.save(room);
    }

    // ----------------------------------------------------------------
    //  Spectators
    // ----------------------------------------------------------------

    public void addSpectator(String roomId, String sessionId) {
        GameRoom room = getRoom(roomId);
        room.getSpectatorSessionIds().add(sessionId);
        roomRepository.save(room);
    }

    public void removeSpectator(String roomId, String sessionId) {
        GameRoom room = getRoom(roomId);
        room.getSpectatorSessionIds().remove(sessionId);
        roomRepository.save(room);
    }

    // ----------------------------------------------------------------
    //  Persistence helpers
    // ----------------------------------------------------------------

    public GameRoom getRoom(String roomId) {
        return roomRepository.findById(roomId)
            .orElseThrow(() ->
                new IllegalArgumentException("Room not found: " + roomId)
            );
    }

    public void saveRoom(GameRoom room) {
        roomRepository.save(room);
    }

    // ----------------------------------------------------------------
    //  Private helpers
    // ----------------------------------------------------------------

    private Player findPlayer(GameRoom room, String playerId) {
        if (room.getWhitePlayer() != null &&
            room.getWhitePlayer().getId().equals(playerId)) {
            return room.getWhitePlayer();
        }
        if (room.getBlackPlayer() != null &&
            room.getBlackPlayer().getId().equals(playerId)) {
            return room.getBlackPlayer();
        }
        throw new IllegalArgumentException(
            "Player " + playerId + " not in room " + room.getRoomId()
        );
    }

    private boolean isOtherPlayerOnline(GameRoom room, String playerId) {
        Player white = room.getWhitePlayer();
        Player black = room.getBlackPlayer();

        if (white != null && !white.getId().equals(playerId) &&
            white.getSessionId() != null) {
            return true;
        }
        if (black != null && !black.getId().equals(playerId) &&
            black.getSessionId() != null) {
            return true;
        }
        return false;
    }
}