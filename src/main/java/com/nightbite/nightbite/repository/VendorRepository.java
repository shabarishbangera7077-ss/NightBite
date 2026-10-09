package com.nightbite.nightbite.repository;

import com.nightbite.nightbite.entity.User;
import com.nightbite.nightbite.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    List<Vendor> findAllByApprovedTrue();
    List<Vendor> findAllByApprovedTrueOrderByShopNameAsc();
    Optional<Vendor> findByOwner(User owner);
}
