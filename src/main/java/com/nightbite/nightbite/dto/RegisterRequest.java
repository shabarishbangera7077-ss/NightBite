package com.nightbite.nightbite.dto;

import com.nightbite.nightbite.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        String password,

        @NotBlank(message = "Phone is required")
        String phone,

        @NotBlank(message = "Hostel block is required")
        String hostelBlock,

        @NotBlank(message = "Room number is required")
        String roomNumber,

        @NotNull(message = "Role is required")
        Role role
) {
}
