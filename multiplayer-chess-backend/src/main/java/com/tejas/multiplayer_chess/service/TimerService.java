package com.tejas.multiplayer_chess.service;

import com.tejas.multiplayer_chess.dto.*;
import com.tejas.multiplayer_chess.engine.ChessEngine;
import com.tejas.multiplayer_chess.model.*;
import com.tejas.multiplayer_chess.repository.GameRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimerService {

    private final GameRoomRepository    roomRepository;
    private final RoomService           roomService;
    private final GameService           gameService;
    private final SimpMessagingTemplate messaging;

    // ----------------------------------------------------------------
    //  Main tick — fires every 1000ms
    // ----------------------------------------------------------------

    /**
     * Every second:
     *  1. Find all IN_PROGRESS rooms
     *  2. Figure out whose clock is running (from FEN active color)
     *  3. Deduct elapsed ms from that player's timer
     *  4. Persist updated room to Redis
     *  5. Broadcast TIMER_UPDATE to both players
     *  6. If clock hits zero → trigger timeout + GAME_OVER broadcast
     *
     * fixedDelay (not fixedRate) means the next tick starts 1000ms
     * AFTER the previous one finishes — no overlapping ticks even if
     * Redis is slow.
     */
  @Scheduled(fixedDelay = 1000)
public void tickAllRooms() {

    Iterable<GameRoom> allRooms = roomRepository.findAll();

    for (GameRoom room : allRooms) {

        // Prevent NullPointerException
        if (room == null) {
            log.warn("Null room found in repository. Skipping...");
            continue;
        }

        // Only process active games
        if (room.getRoomStatus() != RoomStatus.IN_PROGRESS) {
            continue;
        }

        try {

            long now = System.currentTimeMillis();
            long elapsed = now - room.getLastMoveTimestamp();

            ChessEngine engine = new ChessEngine(room.getCurrentFen());
            PlayerColor activeColor = engine.getCurrentTurn();

            if (activeColor == PlayerColor.WHITE) {
                long remaining =
                    room.getWhiteTimeRemainingMs() - elapsed;

                room.setWhiteTimeRemainingMs(
                    Math.max(0, remaining)
                );
            } else {
                long remaining =
                    room.getBlackTimeRemainingMs() - elapsed;

                room.setBlackTimeRemainingMs(
                    Math.max(0, remaining)
                );
            }

            room.setLastMoveTimestamp(now);

            roomRepository.save(room);

            broadcastTimerUpdate(room);

            boolean whiteTimedOut =
                room.getWhiteTimeRemainingMs() <= 0;

            boolean blackTimedOut =
                room.getBlackTimeRemainingMs() <= 0;

            if (whiteTimedOut || blackTimedOut) {

                PlayerColor loser =
                    whiteTimedOut
                        ? PlayerColor.WHITE
                        : PlayerColor.BLACK;

                handleTimeout(room, loser);
            }

        } catch (Exception e) {

            log.error(
                "Timer tick failed for room {}",
                room.getRoomId(),
                e
            );
        }
    }
}
    // ----------------------------------------------------------------
    //  Timeout handling
    // ----------------------------------------------------------------

    /**
     * Called when a player's clock hits zero.
     * Delegates to GameService which marks the room FINISHED
     * and returns the GameOverResponse we broadcast.
     */
    private void handleTimeout(GameRoom room, PlayerColor timedOutColor) {

        log.info("Timeout in room {} — {} ran out of time",
            room.getRoomId(), timedOutColor);

        try {
            GameOverResponse result =
                gameService.processTimeout(room.getRoomId(), timedOutColor);

            messaging.convertAndSend(
                "/topic/game/" + room.getRoomId(),
                WsMessage.builder()
                    .type(MessageType.GAME_OVER)
                    .payload(result)
                    .build()
            );

        } catch (Exception e) {
            // Room may have already been finished by a checkmate
            // that happened in the same tick window — safe to swallow
            log.warn("Timeout handling failed for room {} — already finished? {}",
                room.getRoomId(), e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    //  Timer broadcast
    // ----------------------------------------------------------------

    /**
     * Sends current timer values to /topic/game/{roomId}.
     * Both players and all spectators receive this every second.
     * Frontend uses this to keep its local countdown in sync.
     */
    private void broadcastTimerUpdate(GameRoom room) {
        messaging.convertAndSend(
            "/topic/game/" + room.getRoomId(),
            WsMessage.builder()
                .type(MessageType.TIMER_UPDATE)
                .payload(
                    TimerUpdateResponse.builder()
                        .whiteTimeRemainingMs(room.getWhiteTimeRemainingMs())
                        .blackTimeRemainingMs(room.getBlackTimeRemainingMs())
                        .build()
                )
                .build()
        );
    }
}