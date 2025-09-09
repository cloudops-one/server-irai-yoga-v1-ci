package yoga.irai.server.notification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;
import yoga.irai.server.authentication.entity.DeviceEntity;
import yoga.irai.server.authentication.repository.DeviceRepository;

/**
 * The type Notification controller.
 */
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/notification")
@Tag(name = "Notification Controller", description = "Endpoints for subscribe a Topic")
public class NotificationController {
    private final NotificationService notificationService;
    private final DeviceRepository deviceRepository;

    /**
     * Update fcm token for topic subscription
     *
     * @param notificationTokenDto
     *            the notification token dto
     * @return the response entity
     */
    @PostMapping("/fcm/token")
    @Operation(summary = "Update fcm token for topic subscription", description = "Update fcm token for topic subscription")
    public ResponseEntity<AppResponseDto<Void>> updateFcmToken(
            @Valid @RequestBody NotificationTokenDto notificationTokenDto) {
        DeviceEntity deviceEntity = deviceRepository
                .findByDeviceCodeAndUser(notificationTokenDto.getDeviceCode(), AppUtils.getPrincipalUser())
                .orElse(null);
        if (ObjectUtils.isNotEmpty(deviceEntity)) {
            deviceEntity.setFcmToken(notificationTokenDto.getFcmToken());
            deviceRepository.save(deviceEntity);
        }
        notificationService.subscribeAllTopics(notificationTokenDto.getFcmToken());
        return ResponseEntity
                .ok(AppResponseDto.<Void>builder().message(AppUtils.Messages.FCM_TOKEN_ADDED_SUCCESS).build());
    }
}
