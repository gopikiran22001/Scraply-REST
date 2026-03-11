package com.scraply.rest.repositories;

import com.scraply.rest.models.IllegalDumpingCancellation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IllegalDumpingCancellationRepository extends JpaRepository<IllegalDumpingCancellation, String> {
}
