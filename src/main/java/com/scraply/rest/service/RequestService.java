package com.scraply.rest.service;

import com.scraply.rest.audit.annotation.Auditable;
import com.scraply.rest.dto.request.NewRequestBody;
import com.scraply.rest.dto.request.RequestPickerAssignmentBody;
import com.scraply.rest.dto.request.RequestResponse;
import com.scraply.rest.enums.AuditAction;
import com.scraply.rest.enums.AuditEntityType;
import com.scraply.rest.utilities.RequestUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestUtil requestUtility;

    @Auditable(action = AuditAction.CREATE, entity = AuditEntityType.REQUEST)
    public RequestResponse create(NewRequestBody request) {
        return requestUtility.create(request);
    }


    @Auditable(action = AuditAction.UPDATE, entity = AuditEntityType.REQUEST)
    @Transactional
    public RequestResponse assignPicker(RequestPickerAssignmentBody requestPickerAssignmentBody) {
        return requestUtility.update(requestPickerAssignmentBody);
    }
}
