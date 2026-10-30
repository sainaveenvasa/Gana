package com.gana.service.impl;

import com.gana.dto.AuthResponse;
import com.gana.dto.LoginRequest;
import com.gana.dto.RegisterRequest;
import com.gana.entity.Role;
import com.gana.exception.BadRequestException;
import com.gana.exception.DuplicateResourceException;
import com.gana.exception.InvalidCredentialsException;
import com.gana.exception.TooManyRequestsException;
import com.gana.repository.UserRepository;
import com.gana.security.AppUserDetails;
import com.gana.security.JwtService;
import com.gana.security.LoginAttemptService;
import com.gana.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String INVALID_CREDENTIALS = "Invalid email or password";
    private static final int BCRYPT_MAX_BYTES = 72;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    // Compared against when the email is unknown, so a missing account takes
    // as long to reject as a wrong password (prevents user enumeration by timing).
    private final String dummyPasswordHash;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            LoginAttemptService loginAttemptService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
        this.dummyPasswordHash = passwordEncoder.encode("timing-equalization-dummy-password");
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());

        // @Size counts characters, but BCrypt's limit is in bytes.
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new BadRequestException("Password is too long");
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists");
        }

        com.gana.entity.User user = com.gana.entity.User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        return buildAuthResponse("User registered successfully", user);
    }

    @Override
    public AuthResponse login(LoginRequest request, String clientIp) {
        String email = normalizeEmail(request.getEmail());

        if (loginAttemptService.isBlocked(clientIp, email)) {
            throw new TooManyRequestsException("Too many failed login attempts. Try again later.");
        }

        Optional<com.gana.entity.User> user = userRepository.findByEmail(email);
        String hash = user.map(com.gana.entity.User::getPassword).orElse(dummyPasswordHash);
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), hash);

        if (user.isEmpty() || !passwordMatches) {
            loginAttemptService.recordFailure(clientIp, email);
            throw new InvalidCredentialsException(INVALID_CREDENTIALS);
        }

        loginAttemptService.recordSuccess(clientIp, email);
        return buildAuthResponse("Login successful", user.get());
    }

    @Override
    public void logout(String email) {
        userRepository.incrementTokenVersion(email);
    }

    private AuthResponse buildAuthResponse(String message, com.gana.entity.User user) {
        String token = jwtService.generateToken(new AppUserDetails(user));

        return new AuthResponse(
                message,
                user.getEmail(),
                user.getRole().name(),
                token
        );
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
