package com.tejas.multiplayer_chess.dto;

import lombok.Data;

@Data
public class MakeMoveRequest {
    private String roomId;
    private String playerId;
    private String from;        // "e2"
    private String to;          // "e4"
    private String promotion;   // "q", "r", "b", "n" or null
}