package com.alten.shop.service;

import com.alten.shop.config.JwtUtils;
import com.alten.shop.dto.request.AuthRequest;
import com.alten.shop.dto.request.LoginRequest;
import com.alten.shop.dto.response.AuthResponse;
import com.alten.shop.dto.response.UserResponse;
import com.alten.shop.entity.User;
import com.alten.shop.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Error: Username is already in use!");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setFirstname(request.firstname());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        User savedUser = userRepository.save(user);

        UserResponse userResponse = UserResponse.fromEntity(savedUser);
        String jwt = jwtUtils.generateToken(savedUser);
        return new AuthResponse(jwt, userResponse);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();

        UserResponse userResponse = UserResponse.fromEntity(user);
        String jwt = jwtUtils.generateToken(user);
        return new AuthResponse(jwt, userResponse);
    }
}