package com.tejas.multiplayer_chess.dto;

/**
 * Every WebSocket message has one of these types.
 *
 * CLIENT → SERVER:  JOIN_ROOM, MAKE_MOVE, UNDO_REQUEST, RESIGN, CHAT
 * SERVER → CLIENT:  ROOM_JOINED, GAME_STARTED, MOVE_MADE, MOVE_UNDONE,
 *                   INVALID_MOVE, GAME_OVER, TIMER_UPDATE, ERROR, CHAT
 */
public enum MessageType {

    // Client → Server
    JOIN_ROOM,
    MAKE_MOVE,
    UNDO_REQUEST,
    RESIGN,
    CHAT,

    // Server → Client
    ROOM_JOINED,
    GAME_STARTED,
    MOVE_MADE,
    MOVE_UNDONE,
    INVALID_MOVE,
    GAME_OVER,
    TIMER_UPDATE,
    PLAYER_DISCONNECTED,
    PLAYER_RECONNECTED,
    ERROR,
}