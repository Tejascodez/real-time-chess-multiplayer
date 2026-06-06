package com.tejas.multiplayer_chess.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionRegistry {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final Duration TTL = Duration.ofHours(24);

    public void register(String sessionId, String roomId, String playerId) {
        redisTemplate.opsForValue().set("session:" + sessionId + ":roomId",   roomId,   TTL);
        redisTemplate.opsForValue().set("session:" + sessionId + ":playerId", playerId, TTL);
    }

    public void deregister(String sessionId) {
        redisTemplate.delete("session:" + sessionId + ":roomId");
        redisTemplate.delete("session:" + sessionId + ":playerId");
    }

    public String getRoomId(String sessionId) {
        Object val = redisTemplate.opsForValue().get("session:" + sessionId + ":roomId");
        return val != null ? val.toString() : null;
    }

    public String getPlayerId(String sessionId) {
        Object val = redisTemplate.opsForValue().get("session:" + sessionId + ":playerId");
        return val != null ? val.toString() : null;
    }
}