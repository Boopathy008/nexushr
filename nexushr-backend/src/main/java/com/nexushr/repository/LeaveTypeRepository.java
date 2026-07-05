package com.nexushr.repository;

import com.nexushr.domain.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, UUID> {
    List<LeaveType> findAllByActiveTrue();
    boolean existsByCode(String code);
}
