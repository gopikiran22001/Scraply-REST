package com.scraply.rest.audit.document;

import com.scraply.rest.enums.AuditAction;
import com.scraply.rest.enums.AuditEntityType;
import com.scraply.rest.enums.AuditStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Document(collection = "audit_logs")
@CompoundIndex(name = "idx_audit_user_timestamp", def = "{ 'userId': 1, 'timestamp': -1 }")
@CompoundIndex(name = "idx_audit_entity_timestamp", def = "{ 'entityType': 1, 'entityId': 1, 'timestamp': -1 }")
@CompoundIndex(name = "idx_audit_action_timestamp", def = "{ 'action': 1, 'timestamp': -1 }")
@CompoundIndex(name = "idx_audit_request_id", def = "{ 'requestId': 1 }")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

	@Id
	private String id;

	private UUID userId;

	private AuditAction action;

	private AuditEntityType entityType;

	private UUID entityId;

	private Map<String, Object> oldData;

	private Map<String, Object> newData;

	private String ipAddress;

	private String userAgent;

	private String requestId;

	private AuditStatus status;

	private String errorMessage;

	@Builder.Default
	private Instant timestamp = Instant.now();

}
