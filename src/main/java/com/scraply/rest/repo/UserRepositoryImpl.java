package com.scraply.rest.repo;

import com.scraply.rest.dto.user.PickerResponse;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.enums.AccountStatus;
import com.scraply.rest.enums.UserRole;
import com.scraply.rest.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
class UserRepositoryImpl implements UserQueryRepository {

    private final EntityManager entityManager;

    @Override
    public Page<UserResponse> findPickers(AccountStatus accountStatus, Integer pinCode, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<User> user = query.from(User.class);
        List<Predicate> filters = filters(cb, user, accountStatus, pinCode);

        query.multiselect(
                        user.get("id").alias("id"),
                        user.get("firstName").alias("firstName"),
                        user.get("lastName").alias("lastName"),
                        user.get("email").alias("email"),
                        user.get("phone").alias("phone"),
                        user.get("address").alias("address"),
                        user.get("userRole").alias("userRole"),
                        user.get("pinCode").alias("pinCode"),
                        user.get("vehicleType").alias("vehicleType"),
                        user.get("vehicleNumber").alias("vehicleNumber"),
                        user.get("pickUpRoute").alias("pickUpRoute"),
                        user.get("areaPinCode").alias("areaPinCode"),
                        user.get("profileImage").alias("profileImage"))
                .where(filters.toArray(Predicate[]::new));

        var typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<UserResponse> content = typedQuery.getResultList().stream()
                .map(this::toPickerResponse)
                .toList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> countUser = countQuery.from(User.class);
        countQuery.select(cb.count(countUser))
                .where(filters(cb, countUser, accountStatus, pinCode).toArray(Predicate[]::new));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    private List<Predicate> filters(CriteriaBuilder cb, Root<User> user,
                                    AccountStatus accountStatus, Integer pinCode) {
        List<Predicate> filters = new ArrayList<>();
        filters.add(cb.equal(user.get("userRole"), UserRole.PICKER));
        filters.add(cb.equal(user.get("status"), accountStatus));
        if (pinCode != null) {
            filters.add(cb.equal(user.get("areaPinCode"), pinCode));
        }
        return filters;
    }

    private UserResponse toPickerResponse(Tuple tuple) {
        return new PickerResponse(
                tuple.get("id", java.util.UUID.class),
                tuple.get("firstName", String.class),
                tuple.get("lastName", String.class),
                tuple.get("email", String.class),
                tuple.get("phone", String.class),
                tuple.get("address", String.class),
                tuple.get("userRole", UserRole.class),
                tuple.get("pinCode", Integer.class),
                tuple.get("vehicleType", String.class),
                tuple.get("vehicleNumber", String.class),
                tuple.get("pickUpRoute", String.class),
                tuple.get("areaPinCode", Integer.class),
                tuple.get("profileImage", String.class));
    }
}
