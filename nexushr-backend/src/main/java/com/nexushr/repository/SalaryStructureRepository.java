package com.nexushr.repository;

import com.nexushr.domain.entity.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, UUID> {
    Optional<SalaryStructure> findByEmployeeId(UUID employeeId);
    boolean existsByEmployeeId(UUID employeeId);
}
