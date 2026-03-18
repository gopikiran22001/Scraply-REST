package com.scraply.rest.services;

import com.scraply.rest.dto.CreateQueryRequest;
import com.scraply.rest.dto.ResolveQueryRequest;
import com.scraply.rest.dto.SupportQueryResponse;
import com.scraply.rest.exception.ResourceNotFoundException;
import com.scraply.rest.exception.UnauthorizedException;
import com.scraply.rest.models.SupportQuery;
import com.scraply.rest.models.User;
import com.scraply.rest.models.enums.QueryRequestType;
import com.scraply.rest.models.enums.QueryStatus;
import com.scraply.rest.models.enums.Role;
import com.scraply.rest.repositories.IllegalDumpingRepository;
import com.scraply.rest.repositories.PickupRepository;
import com.scraply.rest.repositories.SupportQueryRepository;
import com.scraply.rest.repositories.UserRepository;
import com.scraply.rest.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportQueryService {

    private final UserRepository userRepository;
    private final PickupRepository pickupRepository;
    private final IllegalDumpingRepository illegalDumpingRepository;
    private final SupportQueryRepository supportQueryRepository;

    private User getCurrentUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public SupportQueryResponse createQuery(CreateQueryRequest request) {
        User user = getCurrentUser();

        if (user.getRole() != Role.USER) {
            throw new UnauthorizedException("Only users can raise queries");
        }

        validateLinkedRequestOwnership(request.getRequestType(), request.getRequestId(), user.getId());

        SupportQuery query = SupportQuery.builder()
                .createdBy(user)
                .requestType(request.getRequestType())
                .requestId(request.getRequestId())
                .subject(request.getSubject().trim())
                .message(request.getMessage().trim())
                .priority(normalizePriority(request.getPriority()))
                .status(QueryStatus.OPEN)
                .build();

        return toResponse(supportQueryRepository.save(query));
    }

    public List<SupportQueryResponse> getQueries(QueryRequestType requestType, QueryStatus status) {
        User user = getCurrentUser();

        List<SupportQuery> queries;
        if (user.getRole() == Role.ADMIN) {
            queries = getAdminScopedQueries(requestType, status);
        } else if (user.getRole() == Role.USER) {
            queries = getUserScopedQueries(user.getId(), requestType, status);
        } else {
            throw new UnauthorizedException("You are not authorized to access queries");
        }

        return queries.stream().map(this::toResponse).toList();
    }

    public SupportQueryResponse resolveQuery(String id, ResolveQueryRequest request) {
        User user = getCurrentUser();

        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only admins can resolve queries");
        }

        SupportQuery query = supportQueryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Query", id));

        query.setStatus(QueryStatus.RESOLVED);
        query.setAdminResponse(request.getAdminResponse().trim());
        query.setResolvedBy(user);
        query.setResolvedAt(LocalDateTime.now());

        return toResponse(supportQueryRepository.save(query));
    }

    private List<SupportQuery> getAdminScopedQueries(QueryRequestType requestType, QueryStatus status) {
        if (requestType != null && status != null) {
            return supportQueryRepository.findByRequestTypeAndStatusOrderByCreatedAtDesc(requestType, status);
        }
        if (requestType != null) {
            return supportQueryRepository.findByRequestTypeOrderByCreatedAtDesc(requestType);
        }
        if (status != null) {
            return supportQueryRepository.findByStatusOrderByCreatedAtDesc(status);
        }
        return supportQueryRepository.findAllByOrderByCreatedAtDesc();
    }

    private List<SupportQuery> getUserScopedQueries(String userId, QueryRequestType requestType, QueryStatus status) {
        if (requestType != null && status != null) {
            return supportQueryRepository.findByCreatedByIdAndRequestTypeAndStatusOrderByCreatedAtDesc(userId, requestType, status);
        }
        if (requestType != null) {
            return supportQueryRepository.findByCreatedByIdAndRequestTypeOrderByCreatedAtDesc(userId, requestType);
        }
        if (status != null) {
            return supportQueryRepository.findByCreatedByIdAndStatusOrderByCreatedAtDesc(userId, status);
        }
        return supportQueryRepository.findByCreatedByIdOrderByCreatedAtDesc(userId);
    }

    private void validateLinkedRequestOwnership(QueryRequestType requestType, String requestId, String userId) {
        if (requestType == QueryRequestType.PICKUP) {
            var pickup = pickupRepository.findByIdAndUserId(requestId, userId);
            if (pickup == null) {
                throw new ResourceNotFoundException("Pickup request", requestId);
            }
            return;
        }

        var dump = illegalDumpingRepository.findByIdAndReporterId(requestId, userId);
        if (dump == null) {
            throw new ResourceNotFoundException("Dump report", requestId);
        }
    }

    private String normalizePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return "NORMAL";
        }
        String normalized = priority.trim().toUpperCase();
        return switch (normalized) {
            case "LOW", "NORMAL", "HIGH", "CRITICAL" -> normalized;
            default -> "NORMAL";
        };
    }

    private SupportQueryResponse toResponse(SupportQuery query) {
        return new SupportQueryResponse(
                query.getId(),
                query.getRequestType(),
                query.getRequestId(),
                query.getSubject(),
                query.getMessage(),
                query.getPriority(),
                query.getStatus(),
                query.getAdminResponse(),
                query.getCreatedBy() != null ? query.getCreatedBy().getId() : null,
                query.getCreatedBy() != null ? query.getCreatedBy().getName() : null,
                query.getResolvedBy() != null ? query.getResolvedBy().getId() : null,
                query.getResolvedBy() != null ? query.getResolvedBy().getName() : null,
                query.getCreatedAt(),
                query.getUpdatedAt(),
                query.getResolvedAt()
        );
    }
}
