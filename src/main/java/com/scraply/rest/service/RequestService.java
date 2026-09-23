package com.scraply.rest.service;

import com.scraply.rest.audit.annotation.Auditable;
import com.scraply.rest.common.PageResponse;
import com.scraply.rest.dto.request.NewRequestBody;
import com.scraply.rest.dto.request.RequestPickerAssignmentBody;
import com.scraply.rest.dto.request.RequestResponse;
import com.scraply.rest.enums.AuditAction;
import com.scraply.rest.enums.AuditEntityType;
import com.scraply.rest.enums.CancellationReason;
import com.scraply.rest.enums.RequestStatus;
import com.scraply.rest.exception.ResourceNotFoundException;
import com.scraply.rest.model.Request;
import com.scraply.rest.repo.RequestRepository;
import com.scraply.rest.utilities.RequestUtility;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestUtility requestUtility;

    private final RequestRepository requestRepository;

    @Auditable(action = AuditAction.CREATE, entity = AuditEntityType.REQUEST)
    public RequestResponse create(NewRequestBody request) {
        return requestUtility.create(request);
    }

    public RequestResponse getRequest(UUID id) {
        Request request =  requestRepository.findById(id).
                orElseThrow(()->new ResourceNotFoundException("Request Not Found"));
        return requestUtility.getRequest(request);
    }

    public PageResponse<RequestResponse> getAllRequests(RequestStatus requestStatus, Integer pinCode, Instant startTime, Instant endTime, int page, int limit) {
        return requestUtility.getAllRequests(requestStatus, pinCode, startTime, endTime, page, limit);
    }

    @Auditable(action = AuditAction.UPDATE, entity = AuditEntityType.REQUEST)
    @Transactional
    public RequestResponse update(UUID id, RequestStatus requestStatus, UUID pickerId, String reason, CancellationReason  cancellationReason, String remarks) {
        Request request =  requestRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Request Not Found"));
        return requestUtility.update(request, requestStatus, pickerId, reason, cancellationReason , remarks);
    }
}
