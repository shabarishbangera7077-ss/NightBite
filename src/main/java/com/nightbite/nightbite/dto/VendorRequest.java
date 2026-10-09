package com.nightbite.nightbite.dto;

import jakarta.validation.constraints.NotBlank;

public record VendorRequest(
        @NotBlank(message = "Shop name is required") String shopName,
        @NotBlank(message = "Open time is required") String openTime,
        @NotBlank(message = "Close time is required") String closeTime
) {
}
