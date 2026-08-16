package com.nomesh.projects.lovable_clone.service.implementation;

import com.nomesh.projects.lovable_clone.dto.auth.AuthResponse;
import com.nomesh.projects.lovable_clone.dto.auth.LoginRequest;
import com.nomesh.projects.lovable_clone.dto.auth.SignupRequest;
import com.nomesh.projects.lovable_clone.entity.User;
import com.nomesh.projects.lovable_clone.exception.BadRequestException;
import com.nomesh.projects.lovable_clone.mapper.AuthMapper;
import com.nomesh.projects.lovable_clone.mapper.UserMapper;
import com.nomesh.projects.lovable_clone.repository.UserRepository;
import com.nomesh.projects.lovable_clone.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    AuthMapper authMapper;
    UserMapper userMapper;

    @Override
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email()))
            throw new BadRequestException("Account already exist with email: " + request.email());

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setUsername(request.email());

        return authMapper.toAuthResponse(
                "dummy_token", userRepository.save(user)
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        return null;
    }
}
