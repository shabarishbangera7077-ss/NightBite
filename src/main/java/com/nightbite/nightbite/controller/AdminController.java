package com.nightbite.nightbite.controller;

import com.nightbite.nightbite.dto.DashboardResponse;
import com.nightbite.nightbite.entity.*;
import com.nightbite.nightbite.exception.BusinessException;
import com.nightbite.nightbite.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BatchRepository batchRepository;

    @PostMapping("/vendors/{id}/approve")
    public ResponseEntity<Vendor> approveVendor(@PathVariable Long id) {
        Vendor vendor = vendorRepository.findById(id).orElseThrow(() -> new BusinessException("Vendor not found"));
        vendor.setApproved(true);
        return ResponseEntity.ok(vendorRepository.save(vendor));
    }

    @PostMapping("/vendors/{id}/reject")
    public ResponseEntity<Vendor> rejectVendor(@PathVariable Long id) {
        Vendor vendor = vendorRepository.findById(id).orElseThrow(() -> new BusinessException("Vendor not found"));
        vendor.setApproved(false);
        return ResponseEntity.ok(vendorRepository.save(vendor));
    }

    @PostMapping("/users/{id}/block")
    public ResponseEntity<User> blockUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new BusinessException("User not found"));
        user.setBlocked(true);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PostMapping("/users/{id}/unblock")
    public ResponseEntity<User> unblockUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new BusinessException("User not found"));
        user.setBlocked(false);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        long totalOrders = orderRepository.count();
        List<String> topItems = new ArrayList<>();
        List<OrderItem> items = orderItemRepository.findAll();
        items.stream()
                .collect(java.util.stream.Collectors.groupingBy(item -> item.getMenuItem().getName(), java.util.stream.Collectors.summingInt(OrderItem::getQuantity)))
                .entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .forEach(entry -> topItems.add(entry.getKey() + " x " + entry.getValue()));

        List<String> busiestHours = new ArrayList<>();
        orderRepository.findAll().stream()
                .map(order -> order.getCreatedAt().getHour())
                .collect(java.util.stream.Collectors.groupingBy(hour -> hour, java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .sorted(java.util.Map.Entry.<Integer, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .forEach(entry -> busiestHours.add(entry.getKey() + ":00"));

        double averageBatchSize = batchRepository.findAll().stream()
                .mapToInt(Batch::getMemberCount)
                .average()
                .orElse(0.0);

        double totalDeliveryFeesSaved = orderRepository.findAll().stream()
                .mapToDouble(order -> Math.max(0, order.getDeliveryFee() - order.getItemsTotal()))
                .sum();

        return new DashboardResponse(totalOrders, topItems, busiestHours, averageBatchSize, totalDeliveryFeesSaved);
    }
}
