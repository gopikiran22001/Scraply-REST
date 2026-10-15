package com.scraply.rest.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final DefaultRedisScript<List> SLIDING_WINDOW_SCRIPT = new DefaultRedisScript<>("""
            local cutoff = tonumber(ARGV[1]) - tonumber(ARGV[2])
            redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, cutoff)
            redis.call('ZADD', KEYS[1], ARGV[1], ARGV[3])
            redis.call('EXPIRE', KEYS[1], math.ceil(tonumber(ARGV[2]) / 1000) + 1)
            local count = redis.call('ZCARD', KEYS[1])
            local oldest = redis.call('ZRANGE', KEYS[1], 0, 0, 'WITHSCORES')
            local retry = 0
            if count > 0 then
                retry = math.max(0, tonumber(oldest[2]) + tonumber(ARGV[2]) - tonumber(ARGV[1]))
            end
            return {count, retry}
            """, List.class);

    private final StringRedisTemplate redisTemplate;

    public Result check(String category, String clientKey, int limit, Duration window) {
        long now = System.currentTimeMillis();
        String key = "scraply:ratelimit:" + category + ":" + clientKey;
        List<Long> values = redisTemplate.execute(
                SLIDING_WINDOW_SCRIPT,
                List.of(key),
                String.valueOf(now),
                String.valueOf(window.toMillis()),
                UUID.randomUUID().toString()
        );

        if (values == null || values.size() < 2) {
            throw new IllegalStateException("Redis rate-limit script returned no result");
        }

        long count = values.get(0);
        long retryAfterMillis = values.get(1);
        return new Result(limit, count, Math.max(0, (retryAfterMillis + 999) / 1000));
    }

    public record Result(int limit, long count, long retryAfterSeconds) {
        public boolean allowed() {
            return count <= limit;
        }

        public long remaining() {
            return Math.max(0, limit - count);
        }
    }
}
