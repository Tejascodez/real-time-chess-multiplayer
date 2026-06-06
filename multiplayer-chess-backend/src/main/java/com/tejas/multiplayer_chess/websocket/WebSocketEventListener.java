package com.tejas.multiplayer_chess.websocket;

import com.tejas.multiplayer_chess.dto.*;
import com.tejas.multiplayer_chess.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messaging;
    private final RoomService           roomService;
    private final SessionRegistry       sessionRegistry;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {

        String sessionId = event.getSessionId();
        log.info("Session disconnected: {}", sessionId);

        // Look up which room this session was in
        String roomId   = sessionRegistry.getRoomId(sessionId);
        String playerId = sessionRegistry.getPlayerId(sessionId);

        if (roomId == null) {
            // Was a spectator or never fully joined
            sessionRegistry.deregister(sessionId);
            return;
        }

        // Notify the other player
        messaging.convertAndSend(
            "/topic/game/" + roomId,
            WsMessage.builder()
                .type(MessageType.PLAYER_DISCONNECTED)
                .payload("Player " + playerId + " disconnected")
                .build()
        );

        // Update room state
        roomService.handleDisconnect(roomId, playerId);

        // Clean up session index
        sessionRegistry.deregister(sessionId);
    }
}