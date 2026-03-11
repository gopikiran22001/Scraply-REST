package com.scraply.rest.controllers;

import com.scraply.rest.dto.PickupRequestBody;
import com.scraply.rest.dto.PickupRequestUpdate;
import com.scraply.rest.models.enums.ScrapCategory;
import com.scraply.rest.services.PickupRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pickups")
@RequiredArgsConstructor
public class PickupRequestController {

    private final PickupRequestService pickupRequestService;

    @GetMapping("/")
    public ResponseEntity<?> getPickupRequests(
            @RequestParam(required = false) ScrapCategory category
    ) {
        if (category != null) {
            return ResponseEntity.ok(pickupRequestService.getPickupRequestsByCategory(category));
        }
        return ResponseEntity.ok(pickupRequestService.getAllPickupRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPickupRequestById(@PathVariable String id) {
        return ResponseEntity.ok(pickupRequestService.getPickupRequestById(id));
    }

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPickupRequest(@ModelAttribute PickupRequestBody pickupRequestBody) {
        return ResponseEntity.ok(pickupRequestService.createPickupRequest(pickupRequestBody));
    }

    @PutMapping("/")
    public ResponseEntity<?> updatePickupRequest(@RequestBody PickupRequestUpdate pickupRequest) {
        return ResponseEntity.ok(pickupRequestService.updatePickupRequest(pickupRequest));
    }

}
