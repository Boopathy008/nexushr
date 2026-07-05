package com.nexushr.repository;

import com.nexushr.domain.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, UUID> {
    List<Designation> findAllByDepartmentIdAndActiveTrue(UUID departmentId);
    boolean existsByTitleAndDepartmentId(String title, UUID departmentId);
}