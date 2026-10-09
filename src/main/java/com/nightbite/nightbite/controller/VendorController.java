package com.nightbite.nightbite.controller;

import com.nightbite.nightbite.dto.MenuItemRequest;
import com.nightbite.nightbite.dto.VendorRequest;
import com.nightbite.nightbite.entity.*;
import com.nightbite.nightbite.exception.BusinessException;
import com.nightbite.nightbite.repository.BatchRepository;
import com.nightbite.nightbite.repository.ReviewRepository;
import com.nightbite.nightbite.repository.VendorRepository;
import com.nightbite.nightbite.service.BatchService;
import com.nightbite.nightbite.service.FeeService;
import com.nightbite.nightbite.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;
    private final BatchService batchService;
    private final BatchRepository batchRepository;
    private final ReviewRepository reviewRepository;
    private final FeeService feeService;
    private final VendorRepository vendorRepository;

    @GetMapping("/vendors")
    public List<Vendor> getApprovedVendors(@RequestParam(required = false) Boolean openNow) {
        return vendorService.getApprovedVendors(openNow);
    }

    @PostMapping("/vendors/register")
    public ResponseEntity<Vendor> registerVendor(@AuthenticationPrincipal User user, @Valid @RequestBody VendorRequest request) {
        return ResponseEntity.ok(vendorService.createVendor(user, request));
    }

    @GetMapping("/vendors/{id}/menu")
    public List<MenuItem> getMenu(@PathVariable Long id,
                                 @RequestParam(required = false) String category,
                                 @RequestParam(required = false) Boolean veg,
                                 @RequestParam(required = false) Double maxPrice) {
        return vendorService.getVendorMenu(id, category, veg, maxPrice);
    }

    @PostMapping("/vendors/{id}/menu")
    public ResponseEntity<MenuItem> addMenuItem(@AuthenticationPrincipal User user,
                                               @PathVariable Long id,
                                               @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(vendorService.addMenuItem(id, user, request));
    }

    @PutMapping("/vendors/{id}/menu/{menuId}")
    public ResponseEntity<MenuItem> updateMenuItem(@AuthenticationPrincipal User user,
                                                  @PathVariable Long id,
                                                  @PathVariable Long menuId,
                                                  @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(vendorService.updateMenuItem(id, menuId, user, request));
    }

    @DeleteMapping("/vendors/{id}/menu/{menuId}")
    public ResponseEntity<Void> deleteMenuItem(@AuthenticationPrincipal User user,
                                              @PathVariable Long id,
                                              @PathVariable Long menuId) {
        vendorService.deleteMenuItem(id, menuId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vendors/{id}/slots")
    public List<Map<String, Object>> getSlots(@PathVariable Long id, @RequestParam(defaultValue = "A") String block) {
        Vendor vendor = vendorService.getVendorById(id);
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDateTime cursor = LocalDateTime.now();

        for (int i = 0; i < 12; i++) {
            LocalDateTime slotTime = cursor.plusMinutes(i * vendor.getSlotMinutes());
            if (!slotTime.toLocalTime().isBefore(vendor.getOpenTime()) && !slotTime.toLocalTime().isAfter(vendor.getCloseTime())) {
                int joined = batchRepository.findByVendorAndHostelBlockAndSlotTime(vendor, block, slotTime)
                        .map(Batch::getMemberCount)
                        .orElse(0);
                int currentFee = feeService.calculateCurrentFee(joined, vendor.getBaseDeliveryFee());
                Map<String, Object> slot = new HashMap<>();
                slot.put("slotTime", slotTime);
                slot.put("cutoffTime", slotTime.minusMinutes(vendor.getCutoffMinutes()));
                slot.put("studentsJoined", joined);
                slot.put("currentFee", currentFee);
                result.add(slot);
            }
        }
        return result;
    }

    @GetMapping("/vendor/batches")
    public List<Batch> getVendorBatches(@AuthenticationPrincipal User user,
                                       @RequestParam(required = false) BatchStatus status) {
        Vendor vendor = vendorRepository.findByOwner(user)
                .orElseThrow(() -> new BusinessException("Only vendors can view batch dashboards"));
        if (status == null) {
            return batchRepository.findByVendor(vendor);
        }
        return batchService.getBatchesForVendor(vendor, status);
    }

    @PatchMapping("/vendor/batches/{id}/status")
    public ResponseEntity<Batch> updateBatchStatus(@AuthenticationPrincipal User user,
                                                  @PathVariable Long id,
                                                  @RequestParam BatchStatus status) {
        Vendor vendor = vendorRepository.findByOwner(user)
                .orElseThrow(() -> new BusinessException("Only vendors can update batch status"));
        Batch batch = batchRepository.findById(id).orElseThrow(() -> new BusinessException("Batch not found"));
        if (!batch.getVendor().getOwner().getId().equals(vendor.getOwner().getId())) {
            throw new BusinessException("You do not own this batch");
        }
        return ResponseEntity.ok(batchService.updateBatchStatus(id, status));
    }

    @GetMapping("/vendors/{id}/reviews")
    public List<Review> getReviews(@PathVariable Long id) {
        Vendor vendor = vendorService.getVendorById(id);
        return reviewRepository.findByVendor(vendor);
    }
}
