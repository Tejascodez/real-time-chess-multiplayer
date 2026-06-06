package com.tejas.multiplayer_chess.dto;

import com.tejas.multiplayer_chess.model.PlayerColor;
import com.tejas.multiplayer_chess.model.RoomStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomJoinedResponse {
    private String roomId;
    private String playerId;
    private PlayerColor assignedColor;
    private RoomStatus roomStatus;
    private String currentFen;
    
}