package com.scraply.rest.controllers;

import com.scraply.rest.dto.CreateQueryRequest;
import com.scraply.rest.dto.ResolveQueryRequest;
import com.scraply.rest.models.enums.QueryRequestType;
import com.scraply.rest.models.enums.QueryStatus;
import com.scraply.rest.services.SupportQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/queries")
@RequiredArgsConstructor
public class SupportQueryController {

    private final SupportQueryService supportQueryService;

    @GetMapping("/")
    public ResponseEntity<?> getQueries(
            @RequestParam(required = false) QueryRequestType requestType,
            @RequestParam(required = false) QueryStatus status
    ) {
        return ResponseEntity.ok(supportQueryService.getQueries(requestType, status));
    }

    @PostMapping("/")
    public ResponseEntity<?> createQuery(@Valid @RequestBody CreateQueryRequest request) {
        return ResponseEntity.ok(supportQueryService.createQuery(request));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolveQuery(
            @PathVariable String id,
            @Valid @RequestBody ResolveQueryRequest request
    ) {
        return ResponseEntity.ok(supportQueryService.resolveQuery(id, request));
    }
}
