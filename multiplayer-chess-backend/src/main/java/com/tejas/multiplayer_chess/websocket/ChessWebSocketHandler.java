package com.tejas.multiplayer_chess.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tejas.multiplayer_chess.dto.*;
import com.tejas.multiplayer_chess.model.*;
import com.tejas.multiplayer_chess.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChessWebSocketHandler {

    private final SimpMessagingTemplate messaging;
    private final GameService           gameService;
    private final RoomService           roomService;
    private final ObjectMapper          objectMapper;
    private final SessionRegistry       sessionRegistry;

    @MessageMapping("/join")
    public void handleJoin(
            @Payload JoinRoomRequest request,
            @Header("simpSessionId") String sessionId
    ) {
        try {
            Player player = Player.builder()
                .id(request.getPlayerId())
                .username(request.getUsername())
                .sessionId(sessionId)
                .build();

            GameRoom room;

            if (request.getRoomId() == null || request.getRoomId().isBlank()) {
                room = roomService.createRoom(player, 600_000);
            } else {
                try {
                    room = roomService.joinRoom(request.getRoomId(), player);
                } catch (IllegalStateException e) {
                    room = roomService.reconnectPlayer(
                        request.getRoomId(),
                        request.getPlayerId(),
                        sessionId
                    );
                }
            }

            sessionRegistry.register(sessionId, room.getRoomId(), request.getPlayerId());

            RoomJoinedResponse response = RoomJoinedResponse.builder()
                .roomId(room.getRoomId())
                .playerId(player.getId())
                .assignedColor(player.getColor())
                .roomStatus(room.getRoomStatus())
                .currentFen(room.getCurrentFen())
                .build();

            sendToSession(
                sessionId,
                "/queue/room",
                WsMessage.builder()
                    .type(MessageType.ROOM_JOINED)
                    .payload(response)
                    .build()
            );

            if (room.getRoomStatus() == RoomStatus.IN_PROGRESS) {
                broadcastToRoom(room.getRoomId(),
                    WsMessage.builder()
                        .type(MessageType.GAME_STARTED)
                        .payload(response)
                        .build()
                );
            }

        } catch (Exception e) {
            log.error("Error handling join", e);
            sendError(sessionId, "Failed to join room: " + e.getMessage());
        }
    }

    @MessageMapping("/move")
    public void handleMove(
            @Payload MakeMoveRequest request,
            @Header("simpSessionId") String sessionId
    ) {
        try {
            MoveMadeResponse result = gameService.processMove(request);

            if (result == null) {
                sendToSession(
                    sessionId,
                    "/queue/errors",
                    WsMessage.builder()
                        .type(MessageType.INVALID_MOVE)
                        .payload("Illegal move: " +
                            request.getFrom() + " → " + request.getTo())
                        .build()
                );
                return;
            }

            broadcastToRoom(request.getRoomId(),
                WsMessage.builder()
                    .type(MessageType.MOVE_MADE)
                    .payload(result)
                    .build()
            );

            if (result.getGameStatus() != GameStatus.ONGOING) {
                broadcastToRoom(request.getRoomId(),
                    WsMessage.builder()
                        .type(MessageType.GAME_OVER)
                        .payload(gameService.buildGameOverResponse(
                            request.getRoomId(),
                            result.getGameStatus()
                        ))
                        .build()
                );
            }

        } catch (Exception e) {
            log.error("Error handling move", e);
            sendError(sessionId, "Move processing failed: " + e.getMessage());
        }
    }

    @MessageMapping("/resign")
    public void handleResign(
            @Payload ResignRequest request,
            @Header("simpSessionId") String sessionId
    ) {
        try {
            GameOverResponse result =
                gameService.processResign(
                    request.getRoomId(),
                    request.getPlayerId()
                );

            broadcastToRoom(request.getRoomId(),
                WsMessage.builder()
                    .type(MessageType.GAME_OVER)
                    .payload(result)
                    .build()
            );

        } catch (Exception e) {
            log.error("Error handling resign", e);
            sendError(sessionId, "Resign failed: " + e.getMessage());
        }
    }

    private void broadcastToRoom(String roomId, WsMessage message) {
        messaging.convertAndSend("/topic/game/" + roomId, message);
    }

    private void sendError(String sessionId, String errorMessage) {
        sendToSession(
            sessionId,
            "/queue/errors",
            WsMessage.builder()
                .type(MessageType.ERROR)
                .payload(errorMessage)
                .build()
        );
    }

    private void sendToSession(String sessionId, String destination, Object payload) {
        SimpMessageHeaderAccessor accessor =
            SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        accessor.setSessionId(sessionId);
        accessor.setLeaveMutable(true);

        messaging.convertAndSendToUser(
            sessionId,
            destination,
            payload,
            accessor.getMessageHeaders()
        );
    }
}