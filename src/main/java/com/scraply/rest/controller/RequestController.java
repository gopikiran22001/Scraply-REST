package com.scraply.rest.controller;

import com.scraply.rest.common.ApiResponse;
import com.scraply.rest.dto.request.NewRequestBody;
import com.scraply.rest.dto.request.RequestPickerAssignmentBody;
import com.scraply.rest.dto.request.RequestResponse;
import com.scraply.rest.service.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/assign-picker")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<ApiResponse<RequestResponse>> assignPicker(@RequestBody RequestPickerAssignmentBody requestPickerAssignmentBody) {
        return ResponseEntity.ok(ApiResponse.success("Picker Assigned", requestService.assignPicker(requestPickerAssignmentBody)));
    }

}
