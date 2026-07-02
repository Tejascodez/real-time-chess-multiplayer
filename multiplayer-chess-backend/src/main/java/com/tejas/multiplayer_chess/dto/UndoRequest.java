package com.tejas.multiplayer_chess.dto;

import lombok.Data;

@Data
public class UndoRequest {
    private String roomId;
    private String playerId;
}
