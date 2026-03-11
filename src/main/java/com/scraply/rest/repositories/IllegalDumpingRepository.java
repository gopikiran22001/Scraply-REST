package com.scraply.rest.repositories;

import com.scraply.rest.dto.IllegalDumpingResponse;
import com.scraply.rest.models.IllegalDumping;
import com.scraply.rest.models.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IllegalDumpingRepository extends JpaRepository<IllegalDumping, String> {

    String DTO_QUERY = """
        SELECT new com.scraply.rest.dto.IllegalDumpingResponse(
            dumping.id,
            reporter.id,
            reporter.name,
            reporter.phone,
            picker.id,
            picker.name,
            picker.phone,
            dumping.description,
            dumping.landmark,
            dumping.imageUrl,
            dumping.latitude,
            dumping.longitude,
            dumping.address,
            dumping.status,
            dumping.reportedAt,
            dumping.assignedAt,
            dumping.resolvedAt
        )
        FROM IllegalDumping dumping
        JOIN dumping.reportedBy reporter
        LEFT JOIN dumping.assignedPicker picker
        """;

    @Query(DTO_QUERY)
    List<IllegalDumpingResponse> findAllDumpingReports();

    @Query(DTO_QUERY + " WHERE dumping.id = :id")
    IllegalDumpingResponse findDumpingById(String id);

    @Query(DTO_QUERY + " WHERE reporter.id = :userId")
    List<IllegalDumpingResponse> findByReporterId(String userId);

    @Query(DTO_QUERY + " WHERE picker.id = :pickerId")
    List<IllegalDumpingResponse> findByPickerId(String pickerId);

    @Query(DTO_QUERY + " WHERE dumping.id = :id AND reporter.id = :userId")
    IllegalDumpingResponse findByIdAndReporterId(String id, String userId);

    @Query(DTO_QUERY + " WHERE dumping.id = :id AND picker.id = :pickerId")
    IllegalDumpingResponse findByIdAndPickerId(String id, String pickerId);

    @Query(DTO_QUERY + " WHERE dumping.status = :status")
    List<IllegalDumpingResponse> findByStatus(Status status);

    @Query(DTO_QUERY + " WHERE reporter.id = :userId AND dumping.status = :status")
    List<?> findByReporterIdAndStatus(Status status, String userId);

    @Query(DTO_QUERY + " WHERE picker.id = :pickerId AND dumping.status = :status")
    List<?> findByPickerIdAndStatus(Status status, String pickerId);
}
