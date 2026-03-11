package com.scraply.rest.controllers;

import com.scraply.rest.dto.IllegalDumpingRequestBody;
import com.scraply.rest.dto.IllegalDumpingUpdate;
import com.scraply.rest.services.IllegalDumpingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/illegals")
@RequiredArgsConstructor
public class IllegalDumpingController {

    private final IllegalDumpingService illegalDumpingService;

    @GetMapping("/")
    public ResponseEntity<?> getDumpingReports() {
        return ResponseEntity.ok(illegalDumpingService.getAllDumpingReports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDumpingReportById(@PathVariable String id) {
        return ResponseEntity.ok(illegalDumpingService.getDumpingReportById(id));
    }

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createDumpingReport(@ModelAttribute IllegalDumpingRequestBody requestBody) {
        return ResponseEntity.ok(illegalDumpingService.createDumpingReport(requestBody));
    }

    @PutMapping("/")
    public ResponseEntity<?> updateDumpingReport(@RequestBody IllegalDumpingUpdate updateRequest) {
        return ResponseEntity.ok(illegalDumpingService.updateDumpingReport(updateRequest));
    }
}
