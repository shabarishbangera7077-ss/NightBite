package com.nightbite.nightbite.service;

import com.nightbite.nightbite.dto.OrderItemRequest;
import com.nightbite.nightbite.dto.OrderRequest;
import com.nightbite.nightbite.entity.*;
import com.nightbite.nightbite.exception.BusinessException;
import com.nightbite.nightbite.exception.ResourceNotFoundException;
import com.nightbite.nightbite.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final VendorService vendorService;
    private final BatchService batchService;
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final FeeService feeService;

    @Transactional
    public Order createOrder(User student, OrderRequest request) {
        Vendor vendor = vendorService.getVendorById(request.vendorId());
        if (!vendor.isApproved()) {
            throw new BusinessException("Vendor is not approved yet");
        }
        if (student.isBlocked()) {
            throw new BusinessException("Student account is blocked");
        }
        if (!vendorService.isOpenNow(vendor)) {
            throw new BusinessException("Vendor is currently closed");
        }

        LocalDateTime slotTime;
        try {
            String normalized = request.slotTime().replace(' ', 'T');
            slotTime = LocalDateTime.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw new BusinessException("Invalid slot time format");
        }

        if (LocalDateTime.now().isAfter(slotTime.minusMinutes(vendor.getCutoffMinutes()))) {
            throw new BusinessException("Slot closed");
        }

        double itemsTotal = 0;
        List<OrderItem> items = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.items()) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.menuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
            if (!Objects.equals(menuItem.getVendor().getId(), vendor.getId())) {
                throw new BusinessException("Item does not belong to this vendor");
            }
            if (!menuItem.isAvailable()) {
                throw new BusinessException("Menu item is unavailable: " + menuItem.getName());
            }
            double lineTotal = menuItem.getPrice() * itemRequest.quantity();
            itemsTotal += lineTotal;

            OrderItem orderItem = OrderItem.builder()
                    .menuItem(menuItem)
                    .quantity(itemRequest.quantity())
                    .price(menuItem.getPrice())
                    .build();
            items.add(orderItem);
        }

        Batch batch = batchService.findOrCreateBatch(vendor, student.getHostelBlock(), slotTime);
        if (batch.getStatus() != BatchStatus.OPEN) {
            throw new BusinessException("This batch is no longer accepting orders");
        }

        if (orderRepository.findByBatchAndStudent(batch, student).isPresent()) {
            throw new BusinessException("You already placed an order in this batch");
        }

        int fee = feeService.calculateCurrentFee(batch.getMemberCount() + 1, vendor.getBaseDeliveryFee());
        Order order = Order.builder()
                .student(student)
                .vendor(vendor)
                .batch(batch)
                .itemsTotal(itemsTotal)
                .deliveryFee(fee)
                .grandTotal(itemsTotal + fee)
                .status(OrderStatus.PLACED)
                .build();

        Order savedOrder = orderRepository.save(order);
        batch.setMemberCount(batch.getMemberCount() + 1);
        batch.setFinalFeePerStudent(feeService.calculateCurrentFee(batch.getMemberCount(), vendor.getBaseDeliveryFee()));

        for (OrderItem item : items) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
        }

        orderRepository.save(savedOrder);
        return savedOrder;
    }

    public List<Order> getStudentOrders(User student) {
        return orderRepository.findByStudentOrderByCreatedAtDesc(student);
    }

    public Order getOrderById(Long orderId, User student) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!Objects.equals(order.getStudent().getId(), student.getId())) {
            throw new BusinessException("You cannot access another student's order");
        }
        return order;
    }

    @Transactional
    public Order cancelOrder(Long orderId, User student) {
        Order order = getOrderById(orderId, student);
        if (order.getBatch() == null || order.getBatch().getStatus() != BatchStatus.OPEN) {
            throw new BusinessException("Only orders in an open batch can be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        Batch batch = order.getBatch();
        batch.setMemberCount(Math.max(0, batch.getMemberCount() - 1));
        batch.setFinalFeePerStudent(feeService.calculateCurrentFee(batch.getMemberCount(), order.getVendor().getBaseDeliveryFee()));
        if (batch.getMemberCount() == 0) {
            batch.setStatus(BatchStatus.CANCELLED);
        }

        order.setDeliveryFee(0);
        order.setGrandTotal(order.getItemsTotal());
        orderRepository.save(order);
        return order;
    }
}
