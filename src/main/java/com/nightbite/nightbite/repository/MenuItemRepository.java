package com.nightbite.nightbite.repository;

import com.nightbite.nightbite.entity.MenuItem;
import com.nightbite.nightbite.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByVendor(Vendor vendor);
    List<MenuItem> findByVendorAndAvailableTrue(Vendor vendor);
    boolean existsByIdAndVendor(Long id, Vendor vendor);
}
