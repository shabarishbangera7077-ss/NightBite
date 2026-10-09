package com.nightbite.nightbite.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MenuItemRequest(
        @NotBlank(message = "Menu item name is required") String name,
        @NotNull(message = "Price is required") @Min(value = 1, message = "Price must be positive") Double price,
        @NotBlank(message = "Category is required") String category,
        boolean veg,
        String imageUrl,
        boolean available
) {
}
