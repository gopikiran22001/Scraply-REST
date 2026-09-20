package com.scraply.rest.audit.annotation;

import com.scraply.rest.audit.document.AuditLog;
import com.scraply.rest.enums.AuditAction;
import com.scraply.rest.enums.AuditEntityType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    AuditAction action();

    AuditEntityType entity();

    String entityId();
}