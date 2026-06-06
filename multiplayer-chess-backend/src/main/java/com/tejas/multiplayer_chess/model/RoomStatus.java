package com.tejas.multiplayer_chess.model;

/**
 * Status of the room itself (lobby lifecycle).
 * Separate from GameStatus which tracks chess outcome.
 *
 * WAITING_FOR_PLAYER  → room created, one player in
 * IN_PROGRESS         → both players joined, game running
 * FINISHED            → game ended (checkmate/draw/timeout)
 * ABANDONED           → both players disconnected
 */
public enum RoomStatus {
    WAITING_FOR_PLAYER,
    IN_PROGRESS,
    FINISHED,
    ABANDONED
}