package com.scraply.rest.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for managing Redis queue operations.
 * Used to enqueue pickup and illegal dump request IDs for processing by AI agents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private static final String PICKUP_QUEUE = "pickup_queue";
    private static final String DUMP_QUEUE = "dump_queue";

    private static final String PICKUP_ASSIGN_QUEUE = "pickup_assign_queue";
    private static final String DUMP_ASSIGN_QUEUE = "dump_assign_queue";

    private final StringRedisTemplate stringRedisTemplate;

    /* ================= PICKUP QUEUE ================= */

    public void enqueuePickupRequest(String pickupRequestId) {
        try {
            stringRedisTemplate.opsForList().leftPush(PICKUP_QUEUE, pickupRequestId);
            log.info("Enqueued pickup request ID {} to queue '{}'", pickupRequestId, PICKUP_QUEUE);
        } catch (Exception e) {
            log.error("Failed to enqueue pickup request ID {} to Redis: {}", pickupRequestId, e.getMessage());
            throw new RuntimeException("Failed to enqueue pickup request to Redis", e);
        }
    }

    public Long getPickupQueueSize() {
        Long size = stringRedisTemplate.opsForList().size(PICKUP_QUEUE);
        return size != null ? size : 0L;
    }

    public String peekNextPickupRequest() {
        return stringRedisTemplate.opsForList().index(PICKUP_QUEUE, -1);
    }

    public String dequeuePickupRequest() {
        return stringRedisTemplate.opsForList().rightPop(PICKUP_QUEUE);
    }


    /* ================= DUMP QUEUE ================= */

    public void enqueueDumpRequest(String dumpRequestId) {
        try {
            stringRedisTemplate.opsForList().leftPush(DUMP_QUEUE, dumpRequestId);
            log.info("Enqueued dump request ID {} to queue '{}'", dumpRequestId, DUMP_QUEUE);
        } catch (Exception e) {
            log.error("Failed to enqueue dump request ID {} to Redis: {}", dumpRequestId, e.getMessage());
            throw new RuntimeException("Failed to enqueue dump request to Redis", e);
        }
    }

    public Long getDumpQueueSize() {
        Long size = stringRedisTemplate.opsForList().size(DUMP_QUEUE);
        return size != null ? size : 0L;
    }

    public String peekNextDumpRequest() {
        return stringRedisTemplate.opsForList().index(DUMP_QUEUE, -1);
    }

    public String dequeueDumpRequest() {
        return stringRedisTemplate.opsForList().rightPop(DUMP_QUEUE);
    }


    /* ================= PICKUP ASSIGN QUEUE ================= */

    public void enqueuePickupForAssignment(String pickupRequestId) {
        try {
            stringRedisTemplate.opsForList().leftPush(PICKUP_ASSIGN_QUEUE, pickupRequestId);
            log.info("Enqueued pickup request ID {} to assignment queue '{}'", pickupRequestId, PICKUP_ASSIGN_QUEUE);
        } catch (Exception e) {
            log.error("Failed to enqueue pickup request ID {} to assignment queue: {}", pickupRequestId, e.getMessage());
            throw new RuntimeException("Failed to enqueue pickup request to assignment queue", e);
        }
    }

    public Long getPickupAssignQueueSize() {
        Long size = stringRedisTemplate.opsForList().size(PICKUP_ASSIGN_QUEUE);
        return size != null ? size : 0L;
    }

    public String peekNextPickupForAssignment() {
        return stringRedisTemplate.opsForList().index(PICKUP_ASSIGN_QUEUE, -1);
    }

    public String dequeuePickupForAssignment() {
        return stringRedisTemplate.opsForList().rightPop(PICKUP_ASSIGN_QUEUE);
    }


    /* ================= DUMP ASSIGN QUEUE ================= */

    public void enqueueDumpForAssignment(String dumpRequestId) {
        try {
            stringRedisTemplate.opsForList().leftPush(DUMP_ASSIGN_QUEUE, dumpRequestId);
            log.info("Enqueued dump request ID {} to assignment queue '{}'", dumpRequestId, DUMP_ASSIGN_QUEUE);
        } catch (Exception e) {
            log.error("Failed to enqueue dump request ID {} to assignment queue: {}", dumpRequestId, e.getMessage());
            throw new RuntimeException("Failed to enqueue dump request to assignment queue", e);
        }
    }

    public Long getDumpAssignQueueSize() {
        Long size = stringRedisTemplate.opsForList().size(DUMP_ASSIGN_QUEUE);
        return size != null ? size : 0L;
    }

    public String peekNextDumpForAssignment() {
        return stringRedisTemplate.opsForList().index(DUMP_ASSIGN_QUEUE, -1);
    }

    public String dequeueDumpForAssignment() {
        return stringRedisTemplate.opsForList().rightPop(DUMP_ASSIGN_QUEUE);
    }
}