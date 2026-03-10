package com.scraply.rest.services;

import com.scraply.rest.cloudinary.CloudinaryService;
import com.scraply.rest.dto.PickupRequestBody;
import com.scraply.rest.dto.PickupRequestUpdate;
import com.scraply.rest.exception.BadRequestException;
import com.scraply.rest.exception.ResourceNotFoundException;
import com.scraply.rest.exception.UnauthorizedException;
import com.scraply.rest.models.PickupCancellation;
import com.scraply.rest.models.PickupRequest;
import com.scraply.rest.models.User;
import com.scraply.rest.models.enums.PickupStatus;
import com.scraply.rest.models.enums.ScrapCategory;
import com.scraply.rest.repositories.PickupCancellationRepository;
import com.scraply.rest.repositories.PickupRequestRepository;
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

    private final PickupRequestRepository pickupRequestRepository;

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
            case USER -> pickupRequestRepository.findByCategoryAndUserId(category, user.getId());
            case PICKER -> pickupRequestRepository.findByCategoryAndPickerId(category, user.getId());
            case ADMIN -> pickupRequestRepository.findAllPickupRequestsByCategory(category);
        };
    }

    public List<?> getAllPickupRequests() {
        User user = getCurrentUser();

        return switch (user.getRole()) {
            case USER -> pickupRequestRepository.findByUserId(user.getId());
            case PICKER -> pickupRequestRepository.findByPickerId(user.getId());
            case ADMIN -> pickupRequestRepository.findAllPickupRequests();
        };
    }

    public Object getPickupRequestById(Long id) {
        User user = getCurrentUser();

        return switch (user.getRole()) {
            case USER -> {
                var result = pickupRequestRepository.findByIdAndUserId(id, user.getId());
                if (result == null) throw new ResourceNotFoundException("Pickup request", id);
                yield result;
            }
            case PICKER -> {
                var result = pickupRequestRepository.findByIdAndPickerId(id, user.getId());
                if (result == null) throw new ResourceNotFoundException("Pickup request", id);
                yield result;
            }
            case ADMIN -> pickupRequestRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Pickup request", id));
        };
    }

    public PickupRequest createPickupRequest(PickupRequestBody pickupRequestBody) {
        User user = getCurrentUser();

        String imageUrl = cloudinaryService.uploadImage(pickupRequestBody.getImage());

        PickupRequest pickupRequest = PickupRequest.builder()
                .user(user)
                .category(pickupRequestBody.getCategory())
                .description(pickupRequestBody.getDescription())
                .imageUrl(imageUrl)
                .address(pickupRequestBody.getAddress())
                .latitude(pickupRequestBody.getLatitude())
                .longitude(pickupRequestBody.getLongitude())
                .build();
        
        PickupRequest savedRequest = pickupRequestRepository.save(pickupRequest);
        
        // Enqueue the pickup request ID to Redis for AI agent processing
        queueService.enqueuePickupRequest(savedRequest.getId());
        
        return savedRequest;
    }

    public String updatePickupRequest(PickupRequestUpdate pickupRequestUpdate) {
        User user = getCurrentUser();

        PickupRequest pickupRequest = pickupRequestRepository.findById(pickupRequestUpdate.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pickup request", pickupRequestUpdate.getId()));

        if (user.getRole() == ADMIN) {
            pickupRequest.setStatus(pickupRequestUpdate.getStatus());
            if (pickupRequestUpdate.getAssignedTo() != null) {
                User picker = userRepository.findById(pickupRequestUpdate.getAssignedTo())
                        .orElseThrow(() -> new ResourceNotFoundException("Picker", pickupRequestUpdate.getAssignedTo()));
                pickupRequest.setPicker(picker);
            }
            pickupRequestRepository.save(pickupRequest);
            return "Updated";
        }

        if (user.getRole() == PICKER) {
            if (pickupRequestUpdate.getStatus() == PickupStatus.COMPLETED) {
                pickupRequest.setStatus(PickupStatus.COMPLETED);
                pickupRequestRepository.save(pickupRequest);
                return "Updated";
            }
            if (pickupRequestUpdate.getStatus() == PickupStatus.CANCELLED) {
                PickupCancellation pickupCancellation = PickupCancellation.builder()
                        .pickupRequest(pickupRequest)
                        .cancelledBy(user)
                        .reason(pickupRequestUpdate.getReason())
                        .build();
                pickupCancellationRepository.save(pickupCancellation);

                pickupRequest.setStatus(PickupStatus.CANCELLED);
                pickupRequestRepository.save(pickupRequest);
                return "Updated";
            }
            throw new BadRequestException("Invalid status update for picker");
        }

        throw new UnauthorizedException("You are not authorized to update this request");
    }

    public List<?> getRequestedPickups() {
        return pickupRequestRepository.findByStatus(PickupStatus.REQUESTED);
    }

    public List<?> getInProgressPickups() {
        return pickupRequestRepository.findByStatus(PickupStatus.REQUESTED);
    }
}
