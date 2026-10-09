package com.nightbite.nightbite.dto;

public record AuthResponse(String token, String role, Long userId, String name) {
}
