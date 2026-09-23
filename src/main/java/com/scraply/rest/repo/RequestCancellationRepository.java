package com.scraply.rest.repo;

import com.scraply.rest.model.RequestCancellation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RequestCancellationRepository extends JpaRepository<RequestCancellation, UUID> {
}
