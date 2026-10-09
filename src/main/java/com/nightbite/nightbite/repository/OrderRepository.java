package com.nightbite.nightbite.repository;

import com.nightbite.nightbite.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStudentOrderByCreatedAtDesc(User student);
    List<Order> findByBatchOrderByCreatedAtAsc(Batch batch);
    List<Order> findByVendorOrderByCreatedAtDesc(Vendor vendor);
    Optional<Order> findByBatchAndStudent(Batch batch, User student);

    @Query("select o from Order o where o.status = :status")
    List<Order> findAllByStatus(OrderStatus status);

    @Query("select o from Order o join o.batch b where b.vendor = :vendor and b.status = :status")
    List<Order> findOrdersForVendorByBatchStatus(Vendor vendor, BatchStatus status);
}
