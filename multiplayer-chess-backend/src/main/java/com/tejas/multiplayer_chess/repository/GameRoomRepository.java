package com.tejas.multiplayer_chess.repository;

import com.tejas.multiplayer_chess.model.GameRoom;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data Redis repository.
 * Gives us save(), findById(), delete() for free.
 * GameRoom TTL is controlled by @RedisHash(timeToLive = 86400).
 */
@Repository
public interface GameRoomRepository
        extends CrudRepository<GameRoom, String> {
}