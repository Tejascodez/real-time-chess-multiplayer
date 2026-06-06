package com.tejas.multiplayer_chess.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic WebSocket message envelope.
 *
 * Every message in both directions uses this shape:
 * {
 *   "type": "MAKE_MOVE",
 *   "payload": { ... }
 * }
 *
 * Payload is Object so we can deserialize into specific DTOs
 * inside the handler based on type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsMessage {
    private MessageType type;
    private Object payload;
}