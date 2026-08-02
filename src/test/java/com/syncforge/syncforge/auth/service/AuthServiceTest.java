package com.syncforge.syncforge.auth.service;

import com.syncforge.syncforge.auth.dto.AuthResponse;
import com.syncforge.syncforge.auth.dto.LoginRequest;
import com.syncforge.syncforge.auth.dto.RegisterRequest;
import com.syncforge.syncforge.auth.model.AppUser;
import com.syncforge.syncforge.auth.model.UserRole;
import com.syncforge.syncforge.auth.repository.AppUserRepository;
import com.syncforge.syncforge.tenant.model.Tenant;
import com.syncforge.syncforge.tenant.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AppUserRepository appUserRepository;
    private TenantRepository tenantRepository;
    private JwtService jwtService;
    private RefreshTokenService refreshTokenService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        tenantRepository = mock(TenantRepository.class);
        jwtService = mock(JwtService.class);
        refreshTokenService = mock(RefreshTokenService.class);

        authService = new AuthService(
                appUserRepository,
                tenantRepository,
                new BCryptPasswordEncoder(),
                jwtService,
                refreshTokenService
        );
    }

    @Test
    void registerShouldHashPasswordAndReturnTokens() {
        Tenant tenant = new Tenant("Acme Technologies");
        ReflectionTestUtils.setField(tenant, "id", 1L);

        when(appUserRepository.existsByEmail("admin@acme.com")).thenReturn(false);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));

        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 10L);
            return user;
        });

        when(jwtService.generateAccessToken(any(AppUser.class))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any(AppUser.class))).thenReturn("refresh-token");

        RegisterRequest request = new RegisterRequest(
                1L,
                "Admin@Acme.com",
                "Admin@12345",
                UserRole.ADMIN
        );

        AuthResponse response = authService.register(request);

        assertEquals(10L, response.userId());
        assertEquals(1L, response.tenantId());
        assertEquals("admin@acme.com", response.email());
        assertEquals(UserRole.ADMIN, response.role());
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());

        verify(appUserRepository).save(argThat(user ->
                user.getEmail().equals("admin@acme.com")
                        && !user.getPasswordHash().equals("Admin@12345")
                        && user.getRole() == UserRole.ADMIN
        ));
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        when(appUserRepository.existsByEmail("admin@acme.com")).thenReturn(true);

        RegisterRequest request = new RegisterRequest(
                1L,
                "admin@acme.com",
                "Admin@12345",
                UserRole.ADMIN
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void loginShouldReturnTokensForCorrectPassword() {
        Tenant tenant = new Tenant("Acme Technologies");
        ReflectionTestUtils.setField(tenant, "id", 1L);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        AppUser user = new AppUser(
                tenant,
                "admin@acme.com",
                encoder.encode("Admin@12345"),
                UserRole.ADMIN
        );
        ReflectionTestUtils.setField(user, "id", 10L);

        AuthService serviceWithSameEncoder = new AuthService(
                appUserRepository,
                tenantRepository,
                encoder,
                jwtService,
                refreshTokenService
        );

        when(appUserRepository.findByEmail("admin@acme.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn("refresh-token");

        LoginRequest request = new LoginRequest(
                "admin@acme.com",
                "Admin@12345"
        );

        AuthResponse response = serviceWithSameEncoder.login(request);

        assertEquals(10L, response.userId());
        assertEquals("admin@acme.com", response.email());
        assertEquals(UserRole.ADMIN, response.role());
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
    }

    @Test
    void loginShouldRejectWrongPassword() {
        Tenant tenant = new Tenant("Acme Technologies");
        ReflectionTestUtils.setField(tenant, "id", 1L);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        AppUser user = new AppUser(
                tenant,
                "admin@acme.com",
                encoder.encode("Admin@12345"),
                UserRole.ADMIN
        );
        ReflectionTestUtils.setField(user, "id", 10L);

        AuthService serviceWithSameEncoder = new AuthService(
                appUserRepository,
                tenantRepository,
                encoder,
                jwtService,
                refreshTokenService
        );

        when(appUserRepository.findByEmail("admin@acme.com")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest(
                "admin@acme.com",
                "WrongPassword123"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> serviceWithSameEncoder.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(jwtService, never()).generateAccessToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }
}
