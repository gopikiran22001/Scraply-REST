package com.scraply.rest.service;

import com.scraply.rest.audit.annotation.Auditable;
import com.scraply.rest.dto.request.RequestBodyDTO;
import com.scraply.rest.dto.request.RequestResponse;
import com.scraply.rest.enums.AuditAction;
import com.scraply.rest.enums.AuditEntityType;
import com.scraply.rest.utilities.RequestUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestUtil requestUtility;

    public RequestResponse create(RequestBodyDTO request) {
        return requestUtility.create(request);
    }
}
