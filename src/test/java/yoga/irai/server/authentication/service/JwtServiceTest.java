package yoga.irai.server.authentication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AddressDto;
import yoga.irai.server.authentication.dto.RefreshTokenRequestDto;
import yoga.irai.server.authentication.dto.SignOutRequestDto;
import yoga.irai.server.authentication.entity.DeviceEntity;
import yoga.irai.server.authentication.entity.RefreshTokenEntity;
import yoga.irai.server.authentication.entity.UserEntity;
import yoga.irai.server.authentication.entity.UserPrincipalEntity;
import yoga.irai.server.authentication.repository.DeviceRepository;
import yoga.irai.server.authentication.repository.RefreshTokenRepository;
import yoga.irai.server.notification.NotificationService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    @InjectMocks
    private JwtService jwtService;
    private RefreshTokenRequestDto refreshTokenRequestDto;
    @Mock
    private DeviceRepository deviceRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private NotificationService notificationService;

    private UUID userId;
    private UserDetails userDetails;
    private String token ;

    private SignOutRequestDto signOutRequestDto;
    @BeforeEach
    void setUp() throws ParseException {
        userId = UUID.randomUUID();
        token = "dummy-token";
        signOutRequestDto = new SignOutRequestDto(userId.toString());
        refreshTokenRequestDto = new RefreshTokenRequestDto();
        refreshTokenRequestDto.setRefreshToken(token);
        UserEntity userEntity = UserEntity.builder()
                .userId(userId)
                .orgId(UUID.randomUUID())
                .userIconStorageId(UUID.randomUUID())
                .userStatus(AppUtils.UserStatus.ACTIVE)
                .userType(AppUtils.UserType.KEYCLOAK_USER)
                .isMobileVerified(true)
                .isEmailVerified(true)
                .userFirstName("Hilton")
                .userLastName("Paul")
                .userEmail("hilton.p@terv.pro")
                .userMobile("+919940798142")
                .gender(AppUtils.Gender.MALE)
                .addresses(List.of(AddressDto.builder()
                        .id(0)
                        .addressLine1("2/1")
                        .addressLine2("Church street")
                        .city("Tirunelveli")
                        .stateProvince("Tamil Nadu")
                        .postalCode("627502")
                        .country("India")
                        .build()))
                .dateOfBirth(new SimpleDateFormat("dd-MM-yyyy").parse("09-06-2003"))
                .bloodGroup(AppUtils.BloodGroup.A_POSITIVE)
                .userAoi("Test")
                .passwordHash("")
                .createdBy(UUID.randomUUID())
                .createdAt(ZonedDateTime.of(2025, 5, 14, 12, 30, 0, 0, ZoneId.of("Asia/Kolkata")))
                .updatedBy(UUID.randomUUID())
                .updatedAt(ZonedDateTime.of(2025, 7, 14, 12, 30, 0, 0, ZoneId.of("Asia/Kolkata")))
                .lastLoginAt(ZonedDateTime.of(2025, 8, 14, 12, 30, 0, 0, ZoneId.of("Asia/Kolkata")))
                .build();
        userDetails = new UserPrincipalEntity(userEntity);
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenUsernameMatchesAndNotExpired() {
        JwtService spyService = spy(new JwtService(deviceRepository, refreshTokenRepository, notificationService));
        doReturn(userId.toString()).when(spyService).extractUsername(token);
        doReturn(false).when(spyService).isTokenExpired(token);
        boolean result = spyService.validateToken(token, userDetails);
        assertTrue(result);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenUsernameDoesNotMatch() {
        JwtService spyService = spy(new JwtService(deviceRepository, refreshTokenRepository, notificationService));
        doReturn("different-id").when(spyService).extractUsername(token);
        boolean result = spyService.validateToken(token, userDetails);
        assertFalse(result);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenExpired() {
        JwtService spyService = spy(new JwtService(deviceRepository, refreshTokenRepository, notificationService));
        doReturn(userId.toString()).when(spyService).extractUsername(token);
        doReturn(true).when(spyService).isTokenExpired(token);
        boolean result = spyService.validateToken(token, userDetails);
        assertFalse(result);
    }
    @Test
    void shouldReturnFalse_WhenTokenNotFound() {
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.empty());
        boolean result = jwtService.isRefreshTokenValid(refreshTokenRequestDto);
        assertFalse(result);
    }
    @Test
    void shouldReturnFalse_WhenTokenExpired() {
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .refreshTokenId(UUID.randomUUID())
                .token(token)
                .refreshTokenStatus(AppUtils.RefreshTokenStatus.ACTIVE)
                .expiresAt(ZonedDateTime.now().minusMinutes(1))
                .build();
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(entity));
        boolean result = jwtService.isRefreshTokenValid(refreshTokenRequestDto);
        assertFalse(result);
    }
    @Test
    void shouldReturnTrue_WhenTokenIsActiveAndNotExpired() {
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .refreshTokenId(UUID.randomUUID())
                .token(token)
                .refreshTokenStatus(AppUtils.RefreshTokenStatus.ACTIVE)
                .expiresAt(ZonedDateTime.now().plusMinutes(10))
                .build();
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(entity));
        boolean result = jwtService.isRefreshTokenValid(refreshTokenRequestDto);
        assertTrue(result);
    }
    @Test
    void shouldReturnFalse_WhenTokenNotActive() {
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .refreshTokenId(UUID.randomUUID())
                .token(token)
                .refreshTokenStatus(AppUtils.RefreshTokenStatus.REVOKED)
                .expiresAt(ZonedDateTime.now().plusMinutes(10))
                .build();
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(entity));
        boolean result = jwtService.isRefreshTokenValid(refreshTokenRequestDto);
        assertFalse(result);
    }

    @Test
    void revokeRefreshTokenInactiveTokenTest() {
        when(refreshTokenRepository.findByUser_UserIdAndRefreshTokenStatus(userId, AppUtils.RefreshTokenStatus.ACTIVE))
                .thenReturn(null);
        var exception = assertThrows(RuntimeException.class,
                () -> jwtService.revokeRefreshToken(signOutRequestDto));
        assertEquals(AppUtils.Messages.REFRESH_TOKEN_NOT_FOUND.getException().getMessage(), exception.getMessage());
        verify(refreshTokenRepository, never()).delete(any());
        verify(deviceRepository, never()).delete(any());
    }
    @Test
    void revokeRefreshTokenActiveTokenTest() {
        DeviceEntity deviceEntity = DeviceEntity.builder().deviceId(UUID.randomUUID()).build();
        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                .refreshTokenId(UUID.randomUUID())
                .refreshTokenStatus(AppUtils.RefreshTokenStatus.ACTIVE)
                .deviceEntity(deviceEntity)
                .build();
        when(refreshTokenRepository.findByUser_UserIdAndRefreshTokenStatus(userId, AppUtils.RefreshTokenStatus.ACTIVE))
                .thenReturn(refreshTokenEntity);
        jwtService.revokeRefreshToken(signOutRequestDto);
        verify(refreshTokenRepository, times(1)).delete(refreshTokenEntity);
        verify(deviceRepository, times(1)).delete(deviceEntity);
    }
}
