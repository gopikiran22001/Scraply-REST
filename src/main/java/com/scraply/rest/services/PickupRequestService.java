package com.scraply.rest.services;

import com.scraply.rest.cloudinary.CloudinaryService;
import com.scraply.rest.dto.PickupRequestBody;
import com.scraply.rest.dto.PickupRequestUpdate;
import com.scraply.rest.exception.BadRequestException;
import com.scraply.rest.exception.ResourceNotFoundException;
import com.scraply.rest.exception.UnauthorizedException;
import com.scraply.rest.models.PickupCancellation;
import com.scraply.rest.models.Pickup;
import com.scraply.rest.models.User;
import com.scraply.rest.models.enums.Status;
import com.scraply.rest.models.enums.ScrapCategory;
import com.scraply.rest.repositories.PickupCancellationRepository;
import com.scraply.rest.repositories.PickupRepository;
import com.scraply.rest.repositories.UserRepository;
import com.scraply.rest.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

import static com.scraply.rest.models.enums.Role.*;

@Service
@RequiredArgsConstructor
public class PickupRequestService {

    private final UserRepository userRepository;

    private final PickupRepository pickupRepository;

    private final PickupCancellationRepository pickupCancellationRepository;

    private final QueueService queueService;

    private final CloudinaryService cloudinaryService;

    private User getCurrentUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public List<?> getPickupRequestsByCategory(ScrapCategory category) {
        User user = getCurrentUser();

        return switch (user.getRole()) {
            case USER -> pickupRepository.findByCategoryAndUserId(category, user.getId());
            case PICKER -> pickupRepository.findByCategoryAndPickerId(category, user.getId());
            case ADMIN -> pickupRepository.findAllPickupRequestsByCategory(category);
            default -> throw new IllegalStateException("Unexpected value: " + user.getRole());
        };
    }

    public List<?> getAllPickupRequests() {
        User user = getCurrentUser();

        return switch (user.getRole()) {
            case USER -> pickupRepository.findByUserId(user.getId());
            case PICKER -> pickupRepository.findByPickerId(user.getId());
            case ADMIN -> pickupRepository.findAllPickupRequests();
            default -> throw new IllegalStateException("Unexpected value: " + user.getRole());
        };
    }

    public Object getPickupRequestById(String id) {
        User user = getCurrentUser();

        return switch (user.getRole()) {
            case USER -> {
                var result = pickupRepository.findByIdAndUserId(id, user.getId());
                if (result == null) throw new ResourceNotFoundException("Pickup request", id);
                yield result;
            }
            case PICKER -> {
                var result = pickupRepository.findByIdAndPickerId(id, user.getId());
                if (result == null) throw new ResourceNotFoundException("Pickup request", id);
                yield result;
            }
            case ADMIN -> pickupRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Pickup request", id));
            default -> throw new IllegalStateException("Unexpected value: " + user.getRole());
        };
    }

    public Pickup createPickupRequest(PickupRequestBody pickupRequestBody) {
        User user = getCurrentUser();

        String imageUrl = cloudinaryService.uploadImage(pickupRequestBody.getImage());

        Pickup pickup = Pickup.builder()
                .user(user)
                .category(pickupRequestBody.getCategory())
                .description(pickupRequestBody.getDescription())
                .imageUrl(imageUrl)
                .address(pickupRequestBody.getAddress())
                .pinCode(pickupRequestBody.getPinCode())
                .latitude(pickupRequestBody.getLatitude())
                .longitude(pickupRequestBody.getLongitude())
                .build();
        
        Pickup savedRequest = pickupRepository.save(pickup);
        
        // Enqueue the pickup request ID to Redis for AI agent processing
        queueService.enqueuePickupRequest(savedRequest.getId());
        
        return savedRequest;
    }

    public String updatePickupRequest(PickupRequestUpdate pickupRequestUpdate) {
        User user = getCurrentUser();

        Pickup pickup = pickupRepository.findById(pickupRequestUpdate.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pickup request", pickupRequestUpdate.getId()));

        if (user.getRole() == ADMIN) {
            pickup.setStatus(pickupRequestUpdate.getStatus());
            if (pickupRequestUpdate.getStatus() == Status.IN_PROGRESS) {
                pickupRepository.save(pickup);
                queueService.enqueuePickupForAssignment(pickup.getId());
                return "Updated";
            } else if(pickupRequestUpdate.getStatus() == Status.REQUESTED) {
                pickupRepository.save(pickup);
                queueService.enqueuePickupRequest(pickup.getId());
                return "Updated";
            } else if (pickupRequestUpdate.getStatus() == Status.ASSIGNED &&
                    pickupRequestUpdate.getAssignedTo() != null) {
                User picker = userRepository.findById(pickupRequestUpdate.getAssignedTo())
                        .orElseThrow(() -> new ResourceNotFoundException("Picker", pickupRequestUpdate.getAssignedTo()));
                pickup.setPicker(picker);
                pickup.setAssignedBy(user);
                pickup.setAssignedAt(java.time.LocalDateTime.now());
                pickup.setPriorityLevel(pickupRequestUpdate.getPriorityLevel());
            } else if (pickupRequestUpdate.getStatus() == Status.CANCELLED) {
                PickupCancellation pickupCancellation = PickupCancellation.builder()
                        .pickup(pickup)
                        .cancelledBy(user)
                        .reason(pickupRequestUpdate.getReason())
                        .build();
                pickupCancellationRepository.save(pickupCancellation);
            }
            pickupRepository.save(pickup);
            return "Updated";
        }

        if (user.getRole() == PICKER) {
            if (pickupRequestUpdate.getStatus() == Status.COMPLETED) {
                pickup.setStatus(Status.COMPLETED);
                pickup.setCompletedAt(java.time.LocalDateTime.now());
                pickupRepository.save(pickup);
                return "Updated";
            }
            if (pickupRequestUpdate.getStatus() == Status.CANCELLED) {
                PickupCancellation pickupCancellation = PickupCancellation.builder()
                        .pickup(pickup)
                        .cancelledBy(user)
                        .reason(pickupRequestUpdate.getReason())
                        .build();
                pickupCancellationRepository.save(pickupCancellation);

                pickup.setStatus(Status.CANCELLED);

                pickupRepository.save(pickup);
                return "Updated";
            }
            throw new BadRequestException("Invalid status update for picker");
        }

        throw new UnauthorizedException("You are not authorized to update this request");
    }

    public List<?> getPickupsByStatus(Status status) {
        User user = getCurrentUser();

        return switch (user.getRole()) {
            case USER -> pickupRepository.findByUserIdAndStatus(status, user.getId());
            case PICKER -> pickupRepository.findByPickerIdAndStatus(status, user.getId());
            case ADMIN -> pickupRepository.findByStatus(status);
            default -> throw new IllegalStateException("Unexpected value: " + user.getRole());
        };
    }

}
