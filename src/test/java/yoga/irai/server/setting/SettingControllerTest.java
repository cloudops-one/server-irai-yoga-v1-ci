package yoga.irai.server.setting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingControllerTest {
    @Mock
    private SettingService settingService;
    @InjectMocks
    private SettingController settingController;
    @Test
    void getCountrySettingTest(){
        SettingEntity settingEntity = SettingEntity.builder()
                .settingId(UUID.randomUUID())
                .settingName("COUNTRY")
                .settingValue("TEXT")
                .build();
        SettingResponseDto settingResponseDto = new SettingResponseDto();
        settingResponseDto.setSettingName("COUNTRY");
        when(settingService.getSettingBySettingName(AppUtils.SettingName.COUNTRY.getSetting()))
                .thenReturn(settingEntity);
        try (MockedStatic<AppUtils> utilities = mockStatic(AppUtils.class)) {
            utilities.when(() -> AppUtils.mapToResponse(settingEntity)).thenReturn(settingResponseDto);
            ResponseEntity<AppResponseDto<SettingResponseDto>> response =
                    settingController.getCountrySetting(AppUtils.SettingName.COUNTRY);
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("COUNTRY", response.getBody().getData().getSettingName());
            assertEquals(AppUtils.Messages.SEARCH_FOUND.getMessage(), response.getBody().getMessage());
        }
    }
}
