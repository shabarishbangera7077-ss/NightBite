package com.nightbite.nightbite.service;

import com.nightbite.nightbite.dto.AuthResponse;
import com.nightbite.nightbite.dto.LoginRequest;
import com.nightbite.nightbite.dto.RegisterRequest;
import com.nightbite.nightbite.entity.Role;
import com.nightbite.nightbite.entity.User;
import com.nightbite.nightbite.exception.BusinessException;
import com.nightbite.nightbite.repository.UserRepository;
import com.nightbite.nightbite.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("User with this email already exists");
        }
        Role role = request.role() == null ? Role.STUDENT : request.role();
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .hostelBlock(request.hostelBlock())
                .roomNumber(request.roomNumber())
                .role(role)
                .blocked(false)
                .build();
        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("Invalid email or password");
        }
        if (user.isBlocked()) {
            throw new BusinessException("Your account is blocked");
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getRole().name(), user.getId(), user.getName());
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
