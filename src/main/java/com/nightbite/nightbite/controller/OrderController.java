package com.nightbite.nightbite.controller;

import com.nightbite.nightbite.dto.OrderRequest;
import com.nightbite.nightbite.entity.Order;
import com.nightbite.nightbite.entity.User;
import com.nightbite.nightbite.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@AuthenticationPrincipal User user,
                                            @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(user, request));
    }

    @GetMapping("/orders/my")
    public List<Order> getMyOrders(@AuthenticationPrincipal User user) {
        return orderService.getStudentOrders(user);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrder(@AuthenticationPrincipal User user,
                                         @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id, user));
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Order> cancelOrder(@AuthenticationPrincipal User user,
                                            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id, user));
    }
}
