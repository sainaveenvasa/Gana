package com.gana.service;

import com.gana.dto.AuthResponse;
import com.gana.dto.LoginRequest;
import com.gana.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
