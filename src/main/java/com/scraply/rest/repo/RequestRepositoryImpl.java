package com.scraply.rest.repo;

import com.scraply.rest.dto.request.RequestResponse;
import com.scraply.rest.enums.RequestCategory;
import com.scraply.rest.enums.RequestStatus;
import com.scraply.rest.enums.RequestType;
import com.scraply.rest.model.Request;
import com.scraply.rest.model.RequestPickerAssignment;
import com.scraply.rest.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class RequestRepositoryImpl implements RequestQueryRepository {

    private final EntityManager entityManager;

    @Override
    public Page<RequestResponse> findAllRequestsForAdmin(RequestStatus status, Integer pinCode,
                                                          Instant startTime, Instant endTime, Pageable pageable) {
        return findRequests(null, null, status, pinCode, startTime, endTime, pageable);
    }

    @Override
    public Page<RequestResponse> findAllRequestsByUser(UUID userId, RequestStatus status, Integer pinCode,
                                                        Instant startTime, Instant endTime, Pageable pageable) {
        return findRequests(userId, null, status, pinCode, startTime, endTime, pageable);
    }

    @Override
    public Page<RequestResponse> findAllRequestsByPicker(UUID pickerId, RequestStatus status, Integer pinCode,
                                                          Instant startTime, Instant endTime, Pageable pageable) {
        return findRequests(null, pickerId, status, pinCode, startTime, endTime, pageable);
    }

    private Page<RequestResponse> findRequests(UUID userId, UUID pickerId, RequestStatus status,
                                               Integer pinCode, Instant startTime, Instant endTime,
                                               Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Request> request = query.from(Request.class);
        Join<Request, RequestPickerAssignment> assignment =
                request.join("requestPickerAssignment", JoinType.LEFT);
        Join<RequestPickerAssignment, User> picker = assignment.join("picker", JoinType.LEFT);

        List<Predicate> predicates = predicates(cb, request, picker, userId, pickerId, status, pinCode, startTime, endTime);
        query.multiselect(
                        request.get("id").alias("id"),
                        request.get("requestType").alias("requestType"),
                        request.get("requestCategory").alias("requestCategory"),
                        request.get("requestStatus").alias("requestStatus"),
                        request.get("description").alias("description"),
                        request.get("imageUrl").alias("imageUrl"),
                        request.get("address").alias("address"),
                        request.get("pinCode").alias("pinCode"),
                        request.get("landMark").alias("landMark"),
                        request.get("latitude").alias("latitude"),
                        request.get("longitude").alias("longitude"),
                        request.get("createdAt").alias("createdAt"),
                        picker.get("id").alias("pickerId"),
                        picker.get("firstName").alias("pickerFirstName"),
                        picker.get("lastName").alias("pickerLastName"),
                        picker.get("vehicleNumber").alias("vehicleNumber"),
                        picker.get("vehicleType").alias("vehicleType"))
                .where(predicates.toArray(Predicate[]::new))
                .orderBy(cb.desc(request.get("createdAt")));

        TypedQuery<Tuple> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<RequestResponse> content = typedQuery.getResultList().stream()
                .map(this::toResponse)
                .toList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Request> countRequest = countQuery.from(Request.class);
        Join<Request, RequestPickerAssignment> countAssignment =
                countRequest.join("requestPickerAssignment", JoinType.LEFT);
        Join<RequestPickerAssignment, User> countPicker = countAssignment.join("picker", JoinType.LEFT);
        countQuery.select(cb.count(countRequest));
        countQuery.where(predicates(cb, countRequest, countPicker, userId, pickerId,
                status, pinCode, startTime, endTime).toArray(Predicate[]::new));

        long total = entityManager.createQuery(countQuery).getSingleResult();
        return new PageImpl<>(content, pageable, total);
    }

    private List<Predicate> predicates(CriteriaBuilder cb, Root<Request> request,
                                       Join<RequestPickerAssignment, User> picker,
                                       UUID userId, UUID pickerId, RequestStatus status,
                                       Integer pinCode, Instant startTime, Instant endTime) {
        List<Predicate> predicates = new ArrayList<>();
        if (userId != null) {
            predicates.add(cb.equal(request.get("user").get("id"), userId));
        }
        if (pickerId != null) {
            predicates.add(cb.equal(picker.get("id"), pickerId));
        }
        if (status != null) {
            predicates.add(cb.equal(request.get("requestStatus"), status));
        }
        if (pinCode != null) {
            predicates.add(cb.equal(request.get("pinCode"), pinCode));
        }
        if (startTime != null) {
            predicates.add(cb.greaterThanOrEqualTo(request.get("createdAt"), startTime));
        }
        if (endTime != null) {
            predicates.add(cb.lessThanOrEqualTo(request.get("createdAt"), endTime));
        }
        return predicates;
    }

    private RequestResponse toResponse(Tuple tuple) {
        return new RequestResponse(
                tuple.get("id", UUID.class),
                tuple.get("requestType", RequestType.class),
                tuple.get("requestCategory", RequestCategory.class),
                tuple.get("requestStatus", RequestStatus.class),
                tuple.get("description", String.class),
                tuple.get("imageUrl", String.class),
                tuple.get("address", String.class),
                tuple.get("pinCode", Integer.class),
                tuple.get("landMark", String.class),
                tuple.get("latitude", Double.class),
                tuple.get("longitude", Double.class),
                tuple.get("createdAt", Instant.class),
                tuple.get("pickerId", UUID.class),
                tuple.get("pickerFirstName", String.class),
                tuple.get("pickerLastName", String.class),
                tuple.get("vehicleNumber", String.class),
                tuple.get("vehicleType", String.class)
        );
    }
}
