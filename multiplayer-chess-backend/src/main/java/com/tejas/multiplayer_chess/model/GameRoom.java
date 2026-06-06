package com.tejas.multiplayer_chess.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "GameRoom", timeToLive = 86400) // 24hr TTL
public class GameRoom implements Serializable {

    @Id
    private String roomId;

    private Player whitePlayer;
    private Player blackPlayer;

    // We store FEN, not the engine object.
    // Engine is reconstructed from FEN on every request.
    @Builder.Default
    private String currentFen =
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    // Full move history as list of UCI strings: "e2e4", "e7e5", ...
    @Builder.Default
    private List<String> moveHistory = new ArrayList<>();

    @Builder.Default
    private RoomStatus roomStatus = RoomStatus.WAITING_FOR_PLAYER;

    // Timer fields — milliseconds remaining per player
    @Builder.Default
    private long whiteTimeRemainingMs = 600_000; // default 10 min

    @Builder.Default
    private long blackTimeRemainingMs = 600_000;

    private long lastMoveTimestamp; // epoch ms — used to deduct time

    private boolean aiGame;

    // Spectator session IDs
    @Builder.Default
    private List<String> spectatorSessionIds = new ArrayList<>();
}