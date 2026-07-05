package com.nexushr.repository.specification;

import com.nexushr.domain.entity.Employee;
import com.nexushr.domain.enums.EmployeeStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EmployeeSpecification {

    private EmployeeSpecification() {}

    public static Specification<Employee> withFilters(
            String search, UUID departmentId,
            UUID designationId, EmployeeStatus status) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("lastName")),  pattern),
                        cb.like(cb.lower(root.get("employeeCode")), pattern),
                        cb.like(cb.lower(root.join("user", JoinType.LEFT).get("email")), pattern)
                ));
            }

            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }

            if (designationId != null) {
                predicates.add(cb.equal(root.get("designation").get("id"), designationId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (query.getResultType() != Long.class) {
                root.fetch("department", JoinType.LEFT);
                root.fetch("designation", JoinType.LEFT);
                root.fetch("user", JoinType.LEFT);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
