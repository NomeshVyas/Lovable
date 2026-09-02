package com.nomesh.projects.lovable_clone.service.implementation;

import com.nomesh.projects.lovable_clone.dto.auth.AuthResponse;
import com.nomesh.projects.lovable_clone.dto.auth.LoginRequest;
import com.nomesh.projects.lovable_clone.dto.auth.SignupRequest;
import com.nomesh.projects.lovable_clone.entity.User;
import com.nomesh.projects.lovable_clone.exception.BadRequestException;
import com.nomesh.projects.lovable_clone.mapper.AuthMapper;
import com.nomesh.projects.lovable_clone.mapper.UserMapper;
import com.nomesh.projects.lovable_clone.repository.UserRepository;
import com.nomesh.projects.lovable_clone.security.AuthUtil;
import com.nomesh.projects.lovable_clone.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    AuthUtil authUtil;
    AuthenticationManager authenticationManager;
    AuthMapper authMapper;
    UserMapper userMapper;

    @Override
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email()))
            throw new BadRequestException("Account already exist with email: " + request.email());
        if (StringUtils.hasText(request.username()) && userRepository.existsByUsername(request.username()))
            throw new BadRequestException("Account already exist with username: " + request.username());

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setUsername(StringUtils.hasText(request.username()) ? request.username() : request.email());
        user = userRepository.save(user);

        return authMapper.toAuthResponse(
                authUtil.generateAccessToken(user), user
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String identifier = StringUtils.hasText(request.email()) ? request.email() : request.username();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, request.password())
        );
        User user = (User) authentication.getPrincipal();

        if (user == null) throw new IllegalStateException("Authenticated user is null");

        return authMapper.toAuthResponse(authUtil.generateAccessToken(user), user);
    }
}
