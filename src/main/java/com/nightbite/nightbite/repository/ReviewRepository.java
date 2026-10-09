package com.nightbite.nightbite.repository;

import com.nightbite.nightbite.entity.Review;
import com.nightbite.nightbite.entity.User;
import com.nightbite.nightbite.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByVendor(Vendor vendor);
    boolean existsByStudentAndVendor(User student, Vendor vendor);
}
