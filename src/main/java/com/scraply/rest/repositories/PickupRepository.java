package com.scraply.rest.repositories;

import com.scraply.rest.dto.PickupRequestResponse;
import com.scraply.rest.models.Pickup;
import com.scraply.rest.models.enums.Status;
import com.scraply.rest.models.enums.ScrapCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PickupRepository extends JpaRepository<Pickup, String> {

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
        FROM Pickup pickup
        JOIN pickup.user user
        LEFT JOIN pickup.picker picker
    """;




    @Query(DTO_QUERY)
    List<PickupRequestResponse> findAllPickupRequests();

    @Query(DTO_QUERY + " WHERE pickup.id = :id AND user.id = :userId")
    PickupRequestResponse findByIdAndUserId(String id, String userId);

    @Query(DTO_QUERY + " WHERE pickup.id = :id AND picker.id = :pickerId")
    PickupRequestResponse findByIdAndPickerId(String id, String pickerId);

    @Query(DTO_QUERY + " WHERE pickup.category = :category")
    List<PickupRequestResponse> findAllPickupRequestsByCategory(ScrapCategory category);


    @Query(DTO_QUERY + " WHERE user.id = :userId")
    List<PickupRequestResponse> findByUserId(String userId);


    @Query(DTO_QUERY + " WHERE user.id = :userId AND pickup.category = :category")
    List<PickupRequestResponse> findByCategoryAndUserId(
            ScrapCategory category,
            String userId
    );


    @Query(DTO_QUERY + " WHERE picker.id = :pickerId")
    List<PickupRequestResponse> findByPickerId(String pickerId);


    @Query(DTO_QUERY + " WHERE pickup.category = :category AND picker.id = :pickerId")
    List<PickupRequestResponse> findByCategoryAndPickerId(
            ScrapCategory category,
            String pickerId
    );

    @Query(DTO_QUERY + " WHERE pickup.status = :status")
    List<PickupRequestResponse> findByStatus(Status status);

    @Query(DTO_QUERY + " WHERE pickup.status = :status AND user.id = :userId")
    List<PickupRequestResponse> findByUserIdAndStatus(Status status, String userId);

    @Query(DTO_QUERY + " WHERE pickup.status = :status AND picker.id = :pickerId")
    List<PickupRequestResponse> findByPickerIdAndStatus(Status status, String pickerId);

}