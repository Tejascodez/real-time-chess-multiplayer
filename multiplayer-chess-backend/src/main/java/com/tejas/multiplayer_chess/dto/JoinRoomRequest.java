package com.tejas.multiplayer_chess.dto;

import lombok.Data;

@Data
public class JoinRoomRequest {
    private String roomId;    // null = create new room
    private String playerId;
    private String username;
}