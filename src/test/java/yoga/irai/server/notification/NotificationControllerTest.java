package yoga.irai.server.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;
import yoga.irai.server.authentication.entity.DeviceEntity;
import yoga.irai.server.authentication.entity.UserEntity;
import yoga.irai.server.authentication.repository.DeviceRepository;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private NotificationController notificationController;

    private UserEntity userEntity;
    private DeviceEntity deviceEntity;
    private NotificationTokenDto notificationTokenDto;

    @BeforeEach
    void setUp() {
        userEntity = UserEntity.builder()
                .userId(UUID.randomUUID())
                .userFirstName("userFirstName").userLastName("userLastName")
                .userMobile("userMobile").userEmail("userEmail")
                .isEmailVerified(true).isMobileVerified(true).orgId(UUID.randomUUID())
                .build();
        deviceEntity = DeviceEntity.builder().deviceId(UUID.randomUUID()).deviceName("deviceName")
                .deviceType("deviceType").deviceCode("deviceCode").fcmToken("fcmToken")
                .user(userEntity)
                .build();
        notificationTokenDto = NotificationTokenDto.builder()
                .deviceCode(deviceEntity.getDeviceCode()).fcmToken("fcmToken")
                .build();
    }

    @Test
    void testUpdateFcmToken_withDeviceEntity() {
        try (MockedStatic<AppUtils> utilities = Mockito.mockStatic(AppUtils.class)) {
            utilities.when(AppUtils::getPrincipalUser).thenReturn(userEntity);
            when(deviceRepository.findByDeviceCodeAndUser(anyString(), any(UserEntity.class))).thenReturn(Optional.ofNullable(deviceEntity));
            doNothing().when(notificationService).subscribeAllTopics(anyString());
            ResponseEntity<AppResponseDto<Void>> response = notificationController.updateFcmToken(notificationTokenDto);
            verify(deviceRepository, times(1)).findByDeviceCodeAndUser(anyString(), any(UserEntity.class));
            assert response.getStatusCode() == HttpStatus.OK;
        }
    }

    @Test
    void testUpdateFcmToken_withOutDeviceEntity() {
        try (MockedStatic<AppUtils> utilities = Mockito.mockStatic(AppUtils.class)) {
            utilities.when(AppUtils::getPrincipalUser).thenReturn(userEntity);
            deviceEntity = null;
            when(deviceRepository.findByDeviceCodeAndUser(anyString(), any(UserEntity.class))).thenReturn(Optional.ofNullable(deviceEntity));
            doNothing().when(notificationService).subscribeAllTopics(anyString());
            ResponseEntity<AppResponseDto<Void>> response = notificationController.updateFcmToken(notificationTokenDto);
            verify(deviceRepository, times(1)).findByDeviceCodeAndUser(anyString(), any(UserEntity.class));
            assert response.getStatusCode() == HttpStatus.OK;
        }
    }
}
