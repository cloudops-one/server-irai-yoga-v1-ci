package yoga.irai.server.setting;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/setting")
@Tag(name = "Setting Management", description = "APIs for managing setting, including creation, retrieval, updating, and deletion.")
public class SettingController {

    private final SettingService settingService;

    /**
     * Retrieves a setting by its name.
     *
     * @param settingName
     *            the name of the setting to be retrieved
     * @return the details of the requested setting
     */
    @GetMapping("/{settingName}")
    @Operation(summary = "Get setting by name", description = "Retrieves a setting by its name. Returns the setting details if found.")
    public ResponseEntity<AppResponseDto<SettingResponseDto>> getCountrySetting(
            @PathVariable AppUtils.SettingName settingName) {
        AppResponseDto.AppResponseDtoBuilder<SettingResponseDto> builder = AppResponseDto.builder();
        SettingEntity settingEntity = settingService.getSettingBySettingName(settingName.getSetting());
        SettingResponseDto settingResponseDto = AppUtils.mapToResponse(settingEntity);
        return ResponseEntity
                .ok(builder.data(settingResponseDto).message(AppUtils.Messages.SEARCH_FOUND.getMessage()).build());
    }
}
