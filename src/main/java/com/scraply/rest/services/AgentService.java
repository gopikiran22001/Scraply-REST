package com.scraply.rest.services;

import com.scraply.rest.dto.AgentDumpingUpdate;
import com.scraply.rest.dto.AgentPickupUpdate;
import com.scraply.rest.exception.ResourceNotFoundException;
import com.scraply.rest.models.IllegalDumping;
import com.scraply.rest.models.IllegalDumpingCancellation;
import com.scraply.rest.models.Pickup;
import com.scraply.rest.models.PickupCancellation;
import com.scraply.rest.models.User;
import com.scraply.rest.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.scraply.rest.models.enums.Status.*;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final PickupRepository pickupRepository;
    private final PickupCancellationRepository pickupCancellationRepository;
    private final IllegalDumpingRepository illegalDumpingRepository;
    private final IllegalDumpingCancellationRepository illegalDumpingCancellationRepository;
    private final UserRepository userRepository;

    private final QueueService queueService;

    private User getAgent(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent", "id", id));
    }

    public Object UpdatePickup(AgentPickupUpdate agentPickupUpdate) {
        User user = getAgent(agentPickupUpdate.getAgentId());

        Pickup pickup = pickupRepository.findById(agentPickupUpdate.getPickupId())
                .orElseThrow(() -> new ResourceNotFoundException("Pickup request", "id", agentPickupUpdate.getPickupId()));

        return switch (agentPickupUpdate.getStatus()) {
            case IN_PROGRESS -> {
                pickup.setStatus(IN_PROGRESS);
                pickup.setAssignedBy(user);
                pickupRepository.save(pickup);

                queueService.enqueuePickupForAssignment(pickup.getId());

                yield "Progressed";
            }
            case ASSIGNED -> {
                User picker = userRepository.findById(agentPickupUpdate.getPickerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Picker", "id", agentPickupUpdate.getPickerId()));
                pickup.setStatus(ASSIGNED);
                pickup.setAssignedBy(user);
                pickup.setPicker(picker);
                pickup.setAssignedAt(LocalDateTime.now());
                pickupRepository.save(pickup);

                yield "Assigned";
            }
            case CANCELLED -> {
                PickupCancellation pickupCancellation = PickupCancellation.builder()
                        .pickup(pickup)
                        .cancelledBy(user)
                        .reason(agentPickupUpdate.getReason())
                        .build();
                pickup.setStatus(CANCELLED);
                pickup.setAssignedBy(user);
                pickupCancellationRepository.save(pickupCancellation);
                pickupRepository.save(pickup);

                yield "Cancelled";
            }
            default -> throw new IllegalStateException("Unexpected value: " + agentPickupUpdate.getStatus());
        };
    }

    public Object UpdateDumping(AgentDumpingUpdate agentDumpingUpdate) {
        User user = getAgent(agentDumpingUpdate.getAgentId());

        IllegalDumping dumping = illegalDumpingRepository.findById(agentDumpingUpdate.getDumpingId())
                .orElseThrow(() -> new ResourceNotFoundException("Illegal dumping report", "id", agentDumpingUpdate.getDumpingId()));

        return switch (agentDumpingUpdate.getStatus()) {
            case IN_PROGRESS -> {
                dumping.setStatus(IN_PROGRESS);
                dumping.setAssignedBy(user);
                illegalDumpingRepository.save(dumping);

                queueService.enqueueDumpForAssignment(dumping.getId());

                yield "Progressed";
            }
            case ASSIGNED -> {
                User picker = userRepository.findById(agentDumpingUpdate.getPickerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Picker", "id", agentDumpingUpdate.getPickerId()));
                dumping.setStatus(ASSIGNED);
                dumping.setAssignedBy(user);
                dumping.setAssignedPicker(picker);
                dumping.setAssignedAt(LocalDateTime.now());
                illegalDumpingRepository.save(dumping);

                yield "Assigned";
            }
            case CANCELLED -> {
                IllegalDumpingCancellation dumpingCancellation = IllegalDumpingCancellation.builder()
                        .illegalDumping(dumping)
                        .cancelledBy(user)
                        .reason(agentDumpingUpdate.getReason())
                        .build();
                dumping.setStatus(CANCELLED);
                dumping.setAssignedBy(user);
                illegalDumpingCancellationRepository.save(dumpingCancellation);
                illegalDumpingRepository.save(dumping);

                yield "Cancelled";
            }
            default -> throw new IllegalStateException("Unexpected value: " + agentDumpingUpdate.getStatus());
        };
    }
}
