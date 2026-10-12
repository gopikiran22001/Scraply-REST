package com.scraply.rest.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisQueueProducer {

    private final StringRedisTemplate redisTemplate;

    public static final String REQUESTED = "requests:requested";
    public static final String IN_PROGRESS = "requests:in-progress";

    public void publishRequested(UUID requestId) {
        redisTemplate.opsForList()
                .rightPush(REQUESTED, requestId.toString());
    }

    public void publishInProgress(UUID requestId) {
        redisTemplate.opsForList()
                .rightPush(IN_PROGRESS, requestId.toString());
    }
}