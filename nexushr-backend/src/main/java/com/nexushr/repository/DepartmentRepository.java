package com.nexushr.repository;

import com.nexushr.domain.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    Optional<Department> findByCode(String code);
    Optional<Department> findByManagerId(UUID managerId);
    boolean existsByName(String name);
    boolean existsByCode(String code);
    List<Department> findAllByActiveTrue();

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.designations WHERE d.active = true")
    List<Department> findAllActiveWithDesignations();
}
