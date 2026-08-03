package com.syncforge.syncforge.auth.service;

import com.syncforge.syncforge.auth.dto.AuthResponse;
import com.syncforge.syncforge.auth.dto.LoginRequest;
import com.syncforge.syncforge.auth.dto.RegisterRequest;
import com.syncforge.syncforge.auth.model.AppUser;
import com.syncforge.syncforge.auth.model.RefreshToken;
import com.syncforge.syncforge.auth.model.UserRole;
import com.syncforge.syncforge.auth.repository.AppUserRepository;
import com.syncforge.syncforge.tenant.model.Tenant;
import com.syncforge.syncforge.tenant.repository.TenantRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            AppUserRepository appUserRepository,
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.appUserRepository = appUserRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (appUserRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));

        UserRole role = request.role() == null ? UserRole.VIEWER : request.role();

        AppUser user = new AppUser(
                tenant,
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                role
        );

        AppUser savedUser = appUserRepository.save(user);

        return createAuthResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        AppUser user = appUserRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is disabled");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(rawRefreshToken);

        refreshToken.revoke();

        AppUser user = refreshToken.getUser();

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is disabled");
        }

        return createAuthResponse(user);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeRefreshToken(rawRefreshToken);
    }

    private AuthResponse createAuthResponse(AppUser user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                user.getId(),
                user.getTenant().getId(),
                user.getEmail(),
                user.getRole(),
                accessToken,
                refreshToken
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
