package com.scraply.rest.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for managing Redis queue operations.
 * Used to enqueue pickup request IDs for processing by AI agents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private static final String PICKUP_QUEUE = "pickup_queue";

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Pushes a pickup request ID to the Redis queue using LPUSH.
     * The ID is added to the left (head) of the list.
     *
     * @param pickupRequestId the ID of the pickup request to enqueue
     */
    public void enqueuePickupRequest(Long pickupRequestId) {
        try {
            stringRedisTemplate.opsForList().leftPush(PICKUP_QUEUE, pickupRequestId.toString());
            log.info("Enqueued pickup request ID {} to queue '{}'", pickupRequestId, PICKUP_QUEUE);
        } catch (Exception e) {
            log.error("Failed to enqueue pickup request ID {} to Redis: {}", pickupRequestId, e.getMessage());
            throw new RuntimeException("Failed to enqueue pickup request to Redis", e);
        }
    }

    /**
     * Gets the current size of the pickup queue.
     *
     * @return the number of items in the queue
     */
    public Long getQueueSize() {
        Long size = stringRedisTemplate.opsForList().size(PICKUP_QUEUE);
        return size != null ? size : 0L;
    }

    /**
     * Peeks at the next item in the queue without removing it.
     * Returns the item from the right (tail) of the list (FIFO order).
     *
     * @return the next pickup request ID or null if queue is empty
     */
    public String peekNextPickupRequest() {
        return stringRedisTemplate.opsForList().index(PICKUP_QUEUE, -1);
    }
}
