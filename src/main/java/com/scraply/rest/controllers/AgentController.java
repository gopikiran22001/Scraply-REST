package com.scraply.rest.controllers;

import com.scraply.rest.exception.UnauthorizedException;
import com.scraply.rest.models.enums.PickupStatus;
import com.scraply.rest.services.PickupRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final PickupRequestService pickupRequestService;

    @GetMapping
    public ResponseEntity<?> getPickups(
            @RequestParam(required = false) PickupStatus status
    ) {

        switch (status) {

            case REQUESTED:
                return ResponseEntity.ok(pickupRequestService.getRequestedPickups());

            case IN_PROGRESS:
                return ResponseEntity.ok(pickupRequestService.getInProgressPickups());

            case null:
                throw new UnauthorizedException("Unauthorized Request!");

            default:
                throw new UnauthorizedException("Unauthorized Request!");
        }
    }

}