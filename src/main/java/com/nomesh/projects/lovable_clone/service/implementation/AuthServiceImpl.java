package com.nomesh.projects.lovable_clone.service.implementation;

import com.nomesh.projects.lovable_clone.dto.auth.AuthResponse;
import com.nomesh.projects.lovable_clone.dto.auth.LoginRequest;
import com.nomesh.projects.lovable_clone.dto.auth.SignupRequest;
import com.nomesh.projects.lovable_clone.entity.User;
import com.nomesh.projects.lovable_clone.mapper.AuthMapper;
import com.nomesh.projects.lovable_clone.repository.UserRepository;
import com.nomesh.projects.lovable_clone.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    AuthMapper authMapper;

    @Override
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email()))
            throw new RuntimeException("Account already exist.");
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(request.password())
                .build();

        return authMapper.toAuthResponse(
                userRepository.save(user)
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        return null;
    }
}
