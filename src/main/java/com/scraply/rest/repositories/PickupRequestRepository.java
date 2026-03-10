package com.scraply.rest.repositories;

import com.scraply.rest.dto.PickupRequestResponse;
import com.scraply.rest.models.PickupRequest;
import com.scraply.rest.models.enums.PickupStatus;
import com.scraply.rest.models.enums.ScrapCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PickupRequestRepository extends JpaRepository<PickupRequest, Long> {

    String DTO_QUERY = """
    SELECT new com.scraply.rest.dto.PickupRequestResponse(
        pickup.id,
        user.id,
        user.name,
        user.phone,
        picker.id,
        picker.name,
        picker.phone,
        pickup.description,
        pickup.category,
        pickup.imageUrl,
        pickup.latitude,
        pickup.longitude,
        pickup.address,
        pickup.status,
        pickup.requestedAt,
        pickup.assignedAt,
        pickup.completedAt
    )
        FROM PickupRequest pickup
        JOIN pickup.user user
        LEFT JOIN pickup.picker picker
    """;




    @Query(DTO_QUERY)
    List<PickupRequestResponse> findAllPickupRequests();

    @Query(DTO_QUERY + "WHERE pickup.id = :id AND user.id = :userId")
    PickupRequestResponse findByIdAndUserId(Long id, Long userId);

    @Query(DTO_QUERY + " WHERE pickup.id = :id AND picker.id = :pickerId")
    PickupRequest findByIdAndPickerId(Long id, Long pickerId);

    @Query(DTO_QUERY + " WHERE pickup.category = :category")
    List<PickupRequestResponse> findAllPickupRequestsByCategory(ScrapCategory category);


    @Query(DTO_QUERY + " WHERE user.id = :userId")
    List<PickupRequestResponse> findByUserId(Long userId);


    @Query(DTO_QUERY + " WHERE user.id = :userId AND pickup.category = :category")
    List<PickupRequestResponse> findByCategoryAndUserId(
            ScrapCategory category,
            Long userId
    );


    @Query(DTO_QUERY + " WHERE picker.id = :pickerId")
    List<PickupRequestResponse> findByPickerId(Long pickerId);


    @Query(DTO_QUERY + " WHERE pickup.category = :category AND picker.id = :pickerId")
    List<PickupRequestResponse> findByCategoryAndPickerId(
            ScrapCategory category,
            Long pickerId
    );

    @Query(DTO_QUERY + " WHERE pickup.status = :pickupStatus")
    List<PickupRequestResponse> findByStatus(PickupStatus pickupStatus);
}