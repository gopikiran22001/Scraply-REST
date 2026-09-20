package com.scraply.rest.repo;

import com.scraply.rest.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository <Request, UUID> {
}
