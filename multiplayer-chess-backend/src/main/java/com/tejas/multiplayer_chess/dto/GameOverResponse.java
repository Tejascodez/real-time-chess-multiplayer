package com.tejas.multiplayer_chess.dto;

import com.tejas.multiplayer_chess.model.GameStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameOverResponse {
    private GameStatus gameStatus;
    private String reason; // "CHECKMATE", "STALEMATE", "TIMEOUT", "RESIGN"
    private String winnerPlayerId; // null for draw
}