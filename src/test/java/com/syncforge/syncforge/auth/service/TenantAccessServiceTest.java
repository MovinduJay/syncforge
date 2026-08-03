package com.syncforge.syncforge.auth.service;

import com.syncforge.syncforge.integration.repository.IntegrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantAccessServiceTest {

    private CurrentUserService currentUserService;
    private IntegrationRepository integrationRepository;
    private TenantAccessService tenantAccessService;

    @BeforeEach
    void setUp() {
        currentUserService = mock(CurrentUserService.class);
        integrationRepository = mock(IntegrationRepository.class);

        tenantAccessService = new TenantAccessService(
                currentUserService,
                integrationRepository
        );
    }

    @Test
    void requireCurrentTenantShouldAllowSameTenant() {
        when(currentUserService.getCurrentTenantId()).thenReturn(1L);

        assertDoesNotThrow(() -> tenantAccessService.requireCurrentTenant(1L));
    }

    @Test
    void requireCurrentTenantShouldBlockDifferentTenant() {
        when(currentUserService.getCurrentTenantId()).thenReturn(1L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tenantAccessService.requireCurrentTenant(2L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals(
                "You cannot access data from another tenant",
                exception.getReason()
        );
    }
}
