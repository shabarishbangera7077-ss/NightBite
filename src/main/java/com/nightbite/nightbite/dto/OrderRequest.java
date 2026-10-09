package com.nightbite.nightbite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequest(
        @NotNull(message = "Vendor id is required") Long vendorId,
        @NotBlank(message = "Slot time is required") String slotTime,
        @NotEmpty(message = "Order items cannot be empty") List<OrderItemRequest> items
) {
}
