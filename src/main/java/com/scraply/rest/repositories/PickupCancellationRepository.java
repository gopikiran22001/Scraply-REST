package com.scraply.rest.repositories;

import com.scraply.rest.models.PickupCancellation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickupCancellationRepository extends JpaRepository<PickupCancellation, Long> {

}
