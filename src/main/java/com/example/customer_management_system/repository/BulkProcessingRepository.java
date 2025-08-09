package com.example.customer_management_system.repository;

import com.example.customer_management_system.entities.BulkProcessing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BulkProcessingRepository extends JpaRepository<BulkProcessing, Long> {

    Optional<BulkProcessing> findByJobId(String jobId);
}
