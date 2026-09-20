package com.scraply.rest.controller;

import com.scraply.rest.common.ApiResponse;
import com.scraply.rest.dto.request.RequestBodyDTO;
import com.scraply.rest.dto.request.RequestResponse;
import com.scraply.rest.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/request")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<RequestResponse>> createRequest(@RequestBody RequestBodyDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Request Created",requestService.create(request)));
    }

}
