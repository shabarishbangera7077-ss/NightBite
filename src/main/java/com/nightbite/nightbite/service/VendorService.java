package com.nightbite.nightbite.service;

import com.nightbite.nightbite.dto.MenuItemRequest;
import com.nightbite.nightbite.dto.VendorRequest;
import com.nightbite.nightbite.entity.MenuItem;
import com.nightbite.nightbite.entity.User;
import com.nightbite.nightbite.entity.Vendor;
import com.nightbite.nightbite.exception.BusinessException;
import com.nightbite.nightbite.exception.ResourceNotFoundException;
import com.nightbite.nightbite.repository.MenuItemRepository;
import com.nightbite.nightbite.repository.UserRepository;
import com.nightbite.nightbite.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    public Vendor getVendorById(Long id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
    }

    public List<Vendor> getApprovedVendors(Boolean openNow) {
        List<Vendor> vendors = vendorRepository.findAllByApprovedTrueOrderByShopNameAsc();
        if (Boolean.TRUE.equals(openNow)) {
            return vendors.stream().filter(v -> isOpenNow(v)).toList();
        }
        return vendors;
    }

    public boolean isOpenNow(Vendor vendor) {
        LocalTime now = LocalTime.now();
        LocalTime open = vendor.getOpenTime();
        LocalTime close = vendor.getCloseTime();

        if (open.equals(close)) {
            return true;
        }

        if (open.isBefore(close)) {
            return !now.isBefore(open) && !now.isAfter(close);
        }

        return !now.isBefore(open) || !now.isAfter(close);
    }

    @Transactional
    public Vendor createVendor(User owner, VendorRequest request) {
        if (owner.getRole() != com.nightbite.nightbite.entity.Role.VENDOR) {
            throw new BusinessException("Only vendors can register a shop");
        }

        Vendor vendor = Vendor.builder()
                .shopName(request.shopName())
                .owner(owner)
                .openTime(LocalTime.parse(request.openTime()))
                .closeTime(LocalTime.parse(request.closeTime()))
                .approved(false)
                .slotMinutes(30)
                .cutoffMinutes(10)
                .baseDeliveryFee(40)
                .minBatchSize(1)
                .build();
        return vendorRepository.save(vendor);
    }

    @Transactional
    public Vendor updateVendorSettings(Long vendorId, User owner, int slotMinutes, int cutoffMinutes, int baseDeliveryFee) {
        Vendor vendor = getVendorById(vendorId);
        if (!Objects.equals(vendor.getOwner().getId(), owner.getId())) {
            throw new BusinessException("You can only update your own shop");
        }
        vendor.setSlotMinutes(slotMinutes);
        vendor.setCutoffMinutes(cutoffMinutes);
        vendor.setBaseDeliveryFee(baseDeliveryFee);
        return vendorRepository.save(vendor);
    }

    public List<MenuItem> getVendorMenu(Long vendorId, String category, Boolean veg, Double maxPrice) {
        Vendor vendor = getVendorById(vendorId);
        List<MenuItem> items = menuItemRepository.findByVendor(vendor);
        return items.stream()
                .filter(item -> category == null || item.getCategory().equalsIgnoreCase(category))
                .filter(item -> veg == null || item.isVeg() == veg)
                .filter(item -> maxPrice == null || item.getPrice() <= maxPrice)
                .toList();
    }

    @Transactional
    public MenuItem addMenuItem(Long vendorId, User owner, MenuItemRequest request) {
        Vendor vendor = getVendorById(vendorId);
        verifyOwner(vendor, owner);
        MenuItem item = MenuItem.builder()
                .vendor(vendor)
                .name(request.name())
                .price(request.price())
                .category(request.category())
                .veg(request.veg())
                .imageUrl(request.imageUrl())
                .available(request.available())
                .build();
        return menuItemRepository.save(item);
    }

    @Transactional
    public MenuItem updateMenuItem(Long vendorId, Long itemId, User owner, MenuItemRequest request) {
        Vendor vendor = getVendorById(vendorId);
        verifyOwner(vendor, owner);
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        if (!Objects.equals(item.getVendor().getId(), vendorId)) {
            throw new BusinessException("You can only edit your own menu items");
        }
        item.setName(request.name());
        item.setPrice(request.price());
        item.setCategory(request.category());
        item.setVeg(request.veg());
        item.setImageUrl(request.imageUrl());
        item.setAvailable(request.available());
        return menuItemRepository.save(item);
    }

    @Transactional
    public void deleteMenuItem(Long vendorId, Long itemId, User owner) {
        Vendor vendor = getVendorById(vendorId);
        verifyOwner(vendor, owner);
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        if (!Objects.equals(item.getVendor().getId(), vendorId)) {
            throw new BusinessException("You can only delete your own menu items");
        }
        menuItemRepository.delete(item);
    }

    public void verifyOwner(Vendor vendor, User owner) {
        if (!Objects.equals(vendor.getOwner().getId(), owner.getId())) {
            throw new BusinessException("You do not own this vendor account");
        }
    }
}
