package com.scraply.rest.repo;

import com.scraply.rest.model.RequestPickerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RequestPickerAssignmentRepository extends JpaRepository<RequestPickerAssignment, UUID> {
}
