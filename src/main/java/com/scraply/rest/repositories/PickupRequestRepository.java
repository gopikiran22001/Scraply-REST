package com.scraply.rest.repositories;

import com.scraply.rest.models.PickupRequest;
import com.scraply.rest.models.enums.ScrapCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PickupRequestRepository extends JpaRepository<PickupRequest, Long> {

    List<PickupRequest> findByCategoryAndUserId(ScrapCategory category, Long userId);

    List<PickupRequest> findByUserId(Long userId);

    List<PickupRequest> findByCategoryAndPickerId(ScrapCategory category, Long pickerId);

    List<PickupRequest> findByPickerId(Long pickerId);

}
