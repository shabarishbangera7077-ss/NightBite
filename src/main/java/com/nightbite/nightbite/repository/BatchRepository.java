package com.nightbite.nightbite.repository;

import com.nightbite.nightbite.entity.Batch;
import com.nightbite.nightbite.entity.BatchStatus;
import com.nightbite.nightbite.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long> {
    Optional<Batch> findByVendorAndHostelBlockAndSlotTime(Vendor vendor, String hostelBlock, LocalDateTime slotTime);
    List<Batch> findByVendorAndStatusOrderBySlotTimeAsc(Vendor vendor, BatchStatus status);
    List<Batch> findByStatusAndCutoffTimeBefore(BatchStatus status, LocalDateTime cutoffTime);
    List<Batch> findByVendor(Vendor vendor);
}
