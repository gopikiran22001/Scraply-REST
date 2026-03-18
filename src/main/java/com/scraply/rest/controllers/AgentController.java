package com.scraply.rest.controllers;

import com.scraply.rest.dto.AgentDumpingUpdate;
import com.scraply.rest.dto.AgentLogRequest;
import com.scraply.rest.dto.AgentPickupUpdate;
import com.scraply.rest.services.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {
    private final AgentService agentService;

    @PutMapping(value = "/pickup")
    public ResponseEntity<?> UpdatePickup(@RequestBody AgentPickupUpdate agentPickupUpdate) {
        return ResponseEntity.ok(agentService.UpdatePickup(agentPickupUpdate));
    }

    @PutMapping(value = "/dumping")
    public ResponseEntity<?> UpdateDumping(@RequestBody AgentDumpingUpdate agentDumpingUpdate) {
        return ResponseEntity.ok(agentService.UpdateDumping(agentDumpingUpdate));
    }

    @PostMapping(value = "/logs")
    public ResponseEntity<?> createLog(@RequestBody AgentLogRequest agentLogRequest) {
        return ResponseEntity.ok(agentService.createAgentLog(agentLogRequest));
    }
}