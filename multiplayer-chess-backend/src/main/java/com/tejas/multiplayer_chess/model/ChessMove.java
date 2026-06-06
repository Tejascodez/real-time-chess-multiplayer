package com.tejas.multiplayer_chess.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChessMove {

    private String from;
    private String to;
    private String promotion;

    private String playerId;

    private long timestamp;
}
    
