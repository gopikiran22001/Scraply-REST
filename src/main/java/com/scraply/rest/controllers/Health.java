package com.scraply.rest.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class Health {

    private final DataSource dataSource;
    private final StringRedisTemplate redisTemplate;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());
        response.put("service", "Scraply REST API");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> healthDetails() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());
        response.put("service", "Scraply REST API");

        Map<String, Object> components = new HashMap<>();
        components.put("database", checkDatabase());
        components.put("redis", checkRedis());
        response.put("components", components);

        // Determine overall status based on component health
        boolean allHealthy = components.values().stream()
                .allMatch(c -> "UP".equals(((Map<?, ?>) c).get("status")));
        response.put("status", allHealthy ? "UP" : "DEGRADED");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> databaseHealth() {
        return ResponseEntity.ok(checkDatabase());
    }

    @GetMapping("/redis")
    public ResponseEntity<Map<String, Object>> redisHealth() {
        return ResponseEntity.ok(checkRedis());
    }

    private Map<String, Object> checkDatabase() {
        Map<String, Object> dbStatus = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5);
            dbStatus.put("status", isValid ? "UP" : "DOWN");
            dbStatus.put("database", connection.getMetaData().getDatabaseProductName());
            dbStatus.put("url", connection.getMetaData().getURL());
        } catch (Exception e) {
            dbStatus.put("status", "DOWN");
            dbStatus.put("error", e.getMessage());
        }
        return dbStatus;
    }

    private Map<String, Object> checkRedis() {
        Map<String, Object> redisStatus = new HashMap<>();
        try {
            var connectionFactory = redisTemplate.getConnectionFactory();
            if (connectionFactory == null) {
                redisStatus.put("status", "DOWN");
                redisStatus.put("error", "Redis connection factory not available");
                return redisStatus;
            }
            String pong = connectionFactory.getConnection().ping();
            redisStatus.put("status", "PONG".equals(pong) ? "UP" : "DOWN");
            redisStatus.put("response", pong);
        } catch (Exception e) {
            redisStatus.put("status", "DOWN");
            redisStatus.put("error", e.getMessage());
        }
        return redisStatus;
    }
}
