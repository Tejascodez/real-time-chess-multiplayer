package com.tejas.multiplayer_chess.dto;

import com.tejas.multiplayer_chess.model.GameStatus;
import com.tejas.multiplayer_chess.model.PlayerColor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UndoResponse {
    private String newFen;          // board after undoing the last move
    private PlayerColor nextTurn;
    private boolean inCheck;
    private GameStatus gameStatus;
    private PlayerColor undoneBy;   // color of the player who used their undo
    private boolean whiteUndoUsed;
    private boolean blackUndoUsed;
}
