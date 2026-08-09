package com.nomesh.projects.lovable_clone.service;

import com.nomesh.projects.lovable_clone.dto.auth.AuthResponse;
import com.nomesh.projects.lovable_clone.dto.auth.LoginRequest;
import com.nomesh.projects.lovable_clone.dto.auth.SignupRequest;

public interface AuthService {

    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);
}
