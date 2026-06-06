package com.tejas.multiplayer_chess.dto;

import com.tejas.multiplayer_chess.model.GameStatus;
import com.tejas.multiplayer_chess.model.PlayerColor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MoveMadeResponse {
    private String from;
    private String to;
    private String promotion;
    private String newFen;          // board after move
    private PlayerColor nextTurn;
    private boolean inCheck;
    private GameStatus gameStatus;  // ONGOING, WHITE_WINS, etc.
    private long whiteTimeRemainingMs;
    private long blackTimeRemainingMs;
}