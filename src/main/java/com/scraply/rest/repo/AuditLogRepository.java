package com.scraply.rest.repo;

import com.scraply.rest.audit.document.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog,String> {

}
