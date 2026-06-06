package com.tejas.multiplayer_chess.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    private String id;

    private String username;

    private String sessionId;

    private PlayerColor color;
}