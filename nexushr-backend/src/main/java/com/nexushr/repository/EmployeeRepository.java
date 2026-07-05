package com.nexushr.repository;

import com.nexushr.domain.entity.Employee;
import com.nexushr.domain.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>,
        JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByUserId(UUID userId);
    Optional<Employee> findByEmployeeCode(String employeeCode);
    boolean existsByEmployeeCode(String employeeCode);

    List<Employee> findAllByDepartmentIdAndStatus(UUID departmentId, EmployeeStatus status);
    long countByStatus(EmployeeStatus status);
    long countByDepartmentId(UUID departmentId);

    @Query("""
        SELECT e FROM Employee e
        JOIN FETCH e.user
        JOIN FETCH e.department
        JOIN FETCH e.designation
        WHERE e.id = :id
        """)
    Optional<Employee> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
        SELECT e FROM Employee e
        JOIN FETCH e.user u
        JOIN FETCH e.department d
        JOIN FETCH e.designation des
        WHERE (:status IS NULL OR e.status = :status)
        AND (:departmentId IS NULL OR d.id = :departmentId)
        """)
    Page<Employee> findAllWithFilters(
            @Param("status") EmployeeStatus status,
            @Param("departmentId") UUID departmentId,
            Pageable pageable);
}
