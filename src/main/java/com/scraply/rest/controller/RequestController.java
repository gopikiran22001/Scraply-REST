package com.scraply.rest.controller;

import com.scraply.rest.common.ApiResponse;
import com.scraply.rest.common.PageResponse;
import com.scraply.rest.dto.request.NewRequestBody;
import com.scraply.rest.dto.request.RequestPickerAssignmentBody;
import com.scraply.rest.dto.request.RequestResponse;
import com.scraply.rest.enums.RequestStatus;
import com.scraply.rest.service.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/request")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<RequestResponse>> createRequest(
            @Valid @ModelAttribute NewRequestBody request
    )  {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Request Created",requestService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RequestResponse>> getRequest(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Request Details", requestService.getRequest(id)));
    }

    @GetMapping("/get-all")
    public ResponseEntity<ApiResponse<PageResponse<RequestResponse>>> getAllRequests(
            @RequestParam(required = false) RequestStatus  requestStatus,
            @RequestParam(required = false) Integer pinCode,
            @RequestParam(required = false) Instant startTime,
            @RequestParam(required = false) Instant endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
            ) {
        return ResponseEntity.ok(ApiResponse.success("Requests List", requestService.getAllRequests(requestStatus, pinCode, startTime, endTime, page, limit)));
    }

    @PutMapping("/request-update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<ApiResponse<RequestResponse>> assignPicker(
            @PathVariable UUID id,
            @RequestParam RequestStatus requestStatus,
            @RequestParam(required = false) UUID pickerId,

            ) {
        return ResponseEntity.ok(ApiResponse.success("Picker Assigned", requestService.assignPicker(requestPickerAssignmentBody)));
    }

}
