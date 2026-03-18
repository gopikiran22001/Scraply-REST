package com.scraply.rest.services;

import com.scraply.rest.dto.AgentDumpingUpdate;
import com.scraply.rest.dto.AgentLogItemResponse;
import com.scraply.rest.dto.AgentLogReportResponse;
import com.scraply.rest.dto.AgentLogRequest;
import com.scraply.rest.dto.AgentPickupUpdate;
import com.scraply.rest.exception.ResourceNotFoundException;
import com.scraply.rest.exception.BadRequestException;
import com.scraply.rest.models.AgentLog;
import com.scraply.rest.models.IllegalDumping;
import com.scraply.rest.models.IllegalDumpingCancellation;
import com.scraply.rest.models.Pickup;
import com.scraply.rest.models.PickupCancellation;
import com.scraply.rest.models.User;
import com.scraply.rest.models.enums.Role;
import com.scraply.rest.repositories.*;
import com.scraply.rest.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.scraply.rest.models.enums.Status.*;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final PickupRepository pickupRepository;
    private final PickupCancellationRepository pickupCancellationRepository;
    private final IllegalDumpingRepository illegalDumpingRepository;
    private final IllegalDumpingCancellationRepository illegalDumpingCancellationRepository;
    private final AgentLogRepository agentLogRepository;
    private final UserRepository userRepository;

    private final QueueService queueService;

    private User getAgent(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent", "id", id));
    }

    private User getCurrentUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
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

            public String createAgentLog(AgentLogRequest request) {
            AgentLog log = AgentLog.builder()
                .agentId(request.getAgentId())
                .level(normalize(request.getLevel(), "INFO", 20))
                .message(normalize(request.getMessage(), "", 2000))
                .eventType(normalize(request.getEventType(), "GENERAL", 100))
                .requestType(normalizeNullable(request.getRequestType(), 50))
                .requestId(normalizeNullable(request.getRequestId(), 80))
                .details(normalizeNullable(request.getDetails(), 4000))
                .build();

            agentLogRepository.save(log);
            return "Agent log saved";
            }

            public AgentLogReportResponse getAgentLogReport(Integer hours, Integer limit) {
            User currentUser = getCurrentUser();
            if (currentUser.getRole() != Role.ADMIN) {
                throw new BadRequestException("Only admins can access agent logs");
            }

            int reportHours = (hours == null || hours <= 0) ? 24 : Math.min(hours, 24 * 30);
            int reportLimit = (limit == null || limit <= 0) ? 50 : Math.min(limit, 200);

            LocalDateTime since = LocalDateTime.now().minusHours(reportHours);

            long totalLogs = agentLogRepository.countByCreatedAtAfter(since);
            long errorLogs = agentLogRepository.countByLevelAndCreatedAtAfter("ERROR", since);
            long warningLogs = agentLogRepository.countByLevelAndCreatedAtAfter("WARNING", since)
                + agentLogRepository.countByLevelAndCreatedAtAfter("WARN", since);

            List<AgentLogItemResponse> recentLogs = agentLogRepository
                .findTop200ByCreatedAtAfterOrderByCreatedAtDesc(since)
                .stream()
                .limit(reportLimit)
                .map(log -> AgentLogItemResponse.builder()
                    .id(log.getId())
                    .agentId(log.getAgentId())
                    .level(log.getLevel())
                    .message(log.getMessage())
                    .eventType(log.getEventType())
                    .requestType(log.getRequestType())
                    .requestId(log.getRequestId())
                    .details(log.getDetails())
                    .createdAt(log.getCreatedAt())
                    .build())
                .collect(Collectors.toList());

            Map<String, Long> logsByEventType = agentLogRepository.countByEventTypeSince(since)
                .stream()
                .collect(Collectors.toMap(
                    row -> row[0] == null ? "UNKNOWN" : String.valueOf(row[0]),
                    row -> (Long) row[1]
                ));

            Map<String, Long> logsByAgent = agentLogRepository.countByAgentSince(since)
                .stream()
                .collect(Collectors.toMap(
                    row -> row[0] == null ? "UNKNOWN" : String.valueOf(row[0]),
                    row -> (Long) row[1]
                ));

            Map<String, Long> sortedEventType = logsByEventType.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (left, right) -> left,
                    LinkedHashMap::new
                ));

            Map<String, Long> sortedByAgent = logsByAgent.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (left, right) -> left,
                    LinkedHashMap::new
                ));

            return AgentLogReportResponse.builder()
                .periodHours(reportHours)
                .totalLogs(totalLogs)
                .errorLogs(errorLogs)
                .warningLogs(warningLogs)
                .logsByEventType(sortedEventType)
                .logsByAgent(sortedByAgent)
                .recentLogs(recentLogs)
                .build();
            }

            private String normalize(String value, String defaultValue, int maxLength) {
            String normalized = value == null ? defaultValue : value.trim();
            if (normalized.isBlank()) {
                normalized = defaultValue;
            }
            if (normalized.length() > maxLength) {
                normalized = normalized.substring(0, maxLength);
            }
            return normalized;
            }

            private String normalizeNullable(String value, int maxLength) {
            if (value == null) {
                return null;
            }
            String normalized = value.trim();
            if (normalized.isBlank()) {
                return null;
            }
            return normalized.length() > maxLength ? normalized.substring(0, maxLength) : normalized;
            }
}
