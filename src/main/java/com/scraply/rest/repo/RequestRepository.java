package com.scraply.rest.repo;

import com.scraply.rest.dto.request.RequestResponse;
import com.scraply.rest.enums.RequestStatus;
import com.scraply.rest.model.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID> {

    // ADMIN
    @Query("""
        SELECT new com.scraply.rest.dto.request.RequestResponse(
            r.id,
            r.requestType,
            r.requestCategory,
            r.requestStatus,
            r.description,
            r.imageUrl,
            r.address,
            r.pinCode,
            r.landMark,
            r.latitude,
            r.longitude,
            r.createdAt,
            p.id,
            p.firstName,
            p.lastName,
            p.vehicleNumber,
            p.vehicleType
        )
        FROM Request r
        LEFT JOIN r.requestPickerAssignment a
        LEFT JOIN a.picker p
        WHERE (:requestStatus IS NULL OR r.requestStatus = :requestStatus)
          AND (:pinCode IS NULL OR r.pinCode = :pinCode)
          AND (:startTime IS NULL OR r.createdAt >= :startTime)
          AND (:endTime IS NULL OR r.createdAt <= :endTime)
        ORDER BY r.createdAt DESC
        """)
    Page<RequestResponse> findAllRequestsForAdmin(
            @Param("requestStatus") RequestStatus requestStatus,
            @Param("pinCode") Integer pinCode,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            Pageable pageable
    );


    // USER
    @Query("""
        SELECT new com.scraply.rest.dto.request.RequestResponse(
            r.id,
            r.requestType,
            r.requestCategory,
            r.requestStatus,
            r.description,
            r.imageUrl,
            r.address,
            r.pinCode,
            r.landMark,
            r.latitude,
            r.longitude,
            r.createdAt,
            p.id,
            p.firstName,
            p.lastName,
            p.vehicleNumber,
            p.vehicleType
        )
        FROM Request r
        LEFT JOIN r.requestPickerAssignment a
        LEFT JOIN a.picker p
        WHERE r.user.id = :userId
          AND (:requestStatus IS NULL OR r.requestStatus = :requestStatus)
          AND (:pinCode IS NULL OR r.pinCode = :pinCode)
          AND (:startTime IS NULL OR r.createdAt >= :startTime)
          AND (:endTime IS NULL OR r.createdAt <= :endTime)
        ORDER BY r.createdAt DESC
        """)
    Page<RequestResponse> findAllRequestsByUser(
            @Param("userId") UUID userId,
            @Param("requestStatus") RequestStatus requestStatus,
            @Param("pinCode") Integer pinCode,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            Pageable pageable
    );


    // PICKER
    @Query("""
        SELECT new com.scraply.rest.dto.request.RequestResponse(
            r.id,
            r.requestType,
            r.requestCategory,
            r.requestStatus,
            r.description,
            r.imageUrl,
            r.address,
            r.pinCode,
            r.landMark,
            r.latitude,
            r.longitude,
            r.createdAt,
            p.id,
            p.firstName,
            p.lastName,
            p.vehicleNumber,
            p.vehicleType
        )
        FROM Request r
        LEFT JOIN r.requestPickerAssignment a
        LEFT JOIN a.picker p
        WHERE a.picker.id = :userId
          AND (:requestStatus IS NULL OR r.requestStatus = :requestStatus)
          AND (:pinCode IS NULL OR r.pinCode = :pinCode)
          AND (:startTime IS NULL OR r.createdAt >= :startTime)
          AND (:endTime IS NULL OR r.createdAt <= :endTime)
        ORDER BY r.createdAt DESC
        """)
    Page<RequestResponse> findAllRequestsByPicker(
            @Param("userId") UUID userId,
            @Param("requestStatus") RequestStatus requestStatus,
            @Param("pinCode") Integer pinCode,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            Pageable pageable
    );
}