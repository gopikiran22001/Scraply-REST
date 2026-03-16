package com.scraply.rest.services;

import com.scraply.rest.cloudinary.CloudinaryService;
import com.scraply.rest.dto.IllegalDumpingRequestBody;
import com.scraply.rest.dto.IllegalDumpingUpdate;
import com.scraply.rest.exception.BadRequestException;
import com.scraply.rest.exception.ResourceNotFoundException;
import com.scraply.rest.exception.UnauthorizedException;
import com.scraply.rest.models.IllegalDumping;
import com.scraply.rest.models.IllegalDumpingCancellation;
import com.scraply.rest.models.User;
import com.scraply.rest.models.enums.Status;
import com.scraply.rest.repositories.IllegalDumpingCancellationRepository;
import com.scraply.rest.repositories.IllegalDumpingRepository;
import com.scraply.rest.repositories.UserRepository;
import com.scraply.rest.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.SchemaToolingSettings;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.scraply.rest.models.enums.Role.*;

@Service
@RequiredArgsConstructor
public class IllegalDumpingService {
    private final IllegalDumpingRepository illegalDumpingRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final QueueService queueService;
    private final IllegalDumpingCancellationRepository illegalDumpingCancellationRepository;

    private User getCurrentUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public List<?> getAllDumpingReports() {
        User user = getCurrentUser();

        return switch (user.getRole()) {
            case USER -> illegalDumpingRepository.findByReporterId(user.getId());
            case PICKER -> illegalDumpingRepository.findByPickerId(user.getId());
            case ADMIN -> illegalDumpingRepository.findAllDumpingReports();
            default -> throw new IllegalStateException("Unexpected value: " + user.getRole());
        };
    }

    public Object getDumpingReportById(String id) {
        User user = getCurrentUser();

        return switch (user.getRole()) {
            case USER -> {
                var result = illegalDumpingRepository.findByIdAndReporterId(id, user.getId());
                if (result == null) throw new ResourceNotFoundException("Illegal dumping report", id);
                yield result;
            }
            case PICKER -> {
                var result = illegalDumpingRepository.findByIdAndPickerId(id, user.getId());
                if (result == null) throw new ResourceNotFoundException("Illegal dumping report", id);
                yield result;
            }
            case ADMIN -> illegalDumpingRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Illegal dumping report", id));
            default -> throw new IllegalStateException("Unexpected value: " + user.getRole());
        };
    }

    public IllegalDumping createDumpingReport(IllegalDumpingRequestBody requestBody) {
        User user = getCurrentUser();

        String imageUrl = cloudinaryService.uploadImage(requestBody.getImage());

        IllegalDumping dumping = IllegalDumping.builder()
                .reportedBy(user)
                .category(requestBody.getCategory())
                .description(requestBody.getDescription())
                .imageUrl(imageUrl)
                .address(requestBody.getAddress())
                .pinCode(requestBody.getPinCode())
                .latitude(requestBody.getLatitude())
                .longitude(requestBody.getLongitude())
                .landmark(requestBody.getLandmark())
                .build();

        IllegalDumping savedReport = illegalDumpingRepository.save(dumping);
        
        // Enqueue the dump report ID to Redis for AI agent processing
        queueService.enqueueDumpRequest(savedReport.getId());
        
        return savedReport;
    }

    public String updateDumpingReport(IllegalDumpingUpdate updateRequest) {
        User user = getCurrentUser();

        IllegalDumping dumping = illegalDumpingRepository.findById(updateRequest.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Illegal dumping report", updateRequest.getId()));

        if (user.getRole() == ADMIN) {
            dumping.setStatus(updateRequest.getStatus());
            if(updateRequest.getStatus() == Status.IN_PROGRESS) {
                queueService.enqueueDumpForAssignment(dumping.getId());
            } else if ( updateRequest.getStatus() == Status.ASSIGNED &&
                    updateRequest.getAssignedTo() != null) {
                User picker = userRepository.findById(updateRequest.getAssignedTo())
                        .orElseThrow(() -> new ResourceNotFoundException("Picker", updateRequest.getAssignedTo()));
                dumping.setAssignedPicker(picker);
                dumping.setPriorityLevel(updateRequest.getPriorityLevel());
                dumping.setAssignedBy(user);
                dumping.setAssignedAt(LocalDateTime.now());
            } else if (updateRequest.getStatus() == Status.COMPLETED) {
                dumping.setResolvedAt(LocalDateTime.now());
            } else if (updateRequest.getStatus() == Status.CANCELLED) {
                IllegalDumpingCancellation cancellation = IllegalDumpingCancellation.builder()
                        .illegalDumping(dumping)
                        .cancelledBy(user)
                        .reason(updateRequest.getReason())
                        .build();
                illegalDumpingCancellationRepository.save(cancellation);
            }
            illegalDumpingRepository.save(dumping);
            return "Updated";
        }

        if (user.getRole() == PICKER) {
            if (updateRequest.getStatus() == Status.COMPLETED) {
                dumping.setStatus(Status.COMPLETED);
                dumping.setResolvedAt(LocalDateTime.now());
                illegalDumpingRepository.save(dumping);
                return "Updated";
            }
            if (updateRequest.getStatus() == Status.CANCELLED) {
                IllegalDumpingCancellation cancellation = IllegalDumpingCancellation.builder()
                        .illegalDumping(dumping)
                        .cancelledBy(user)
                        .reason(updateRequest.getReason())
                        .build();
                illegalDumpingCancellationRepository.save(cancellation);

                dumping.setStatus(Status.CANCELLED);
                illegalDumpingRepository.save(dumping);
                return "Updated";
            }
            throw new BadRequestException("Invalid status update for picker");
        }

        throw new UnauthorizedException("You are not authorized to update this report");
    }

    public List<?> getDumpingReportByStatus(Status status) {
        User user = getCurrentUser();

        return switch (user.getRole()) {
            case USER -> illegalDumpingRepository.findByReporterIdAndStatus(status, user.getId());
            case PICKER -> illegalDumpingRepository.findByPickerIdAndStatus(status, user.getId());
            case ADMIN -> illegalDumpingRepository.findByStatus(status);
            default -> throw new IllegalStateException("Unexpected value: " + user.getRole());
        };
    }
}
