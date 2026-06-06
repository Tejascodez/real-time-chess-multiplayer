package com.tejas.multiplayer_chess.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimerUpdateResponse {
    private long whiteTimeRemainingMs;
    private long blackTimeRemainingMs;
}