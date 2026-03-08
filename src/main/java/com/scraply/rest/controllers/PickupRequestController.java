package com.scraply.rest.controllers;

import com.scraply.rest.models.enums.ScrapCategory;
import com.scraply.rest.services.PickupRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pickups")
@RequiredArgsConstructor
public class PickupRequestController {

    private final PickupRequestService pickupRequestService;

    @GetMapping("/")
    public ResponseEntity<?> getAllPickupRequests(){
        return pickupRequestService.getAllPickupRequests();
    }

    @GetMapping("/{category}")
    public ResponseEntity<?> getPickupRequestsByCategory(@PathVariable ScrapCategory category){
        return pickupRequestService.getPickupRequestsByCategory(category);
    }


}
