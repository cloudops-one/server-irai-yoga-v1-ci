package yoga.irai.server.setting;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yoga.irai.server.app.AppUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SettingServiceTest {
    @Mock
    private SettingRepository settingRepository;

    @InjectMocks
    private SettingService settingService;

    @Test
    void testGetSettingBySettingNameFound() {
        SettingEntity settingEntity = SettingEntity.builder()
                .settingName("COUNTRY")
                .settingValue("India")
                .build();
        when(settingRepository.findBySettingName("COUNTRY")).thenReturn(Optional.of(settingEntity));
        SettingEntity result = settingService.getSettingBySettingName("COUNTRY");
        assertNotNull(result);
        assertEquals("COUNTRY", result.getSettingName());
        assertEquals("India", result.getSettingValue());
    }
    @Test
    void testUpdateSettingAddsOldTags()  {
        Set<String> oldTags = new HashSet<>(Set.of("tag1", "tag2"));
        String oldTagsJson = AppUtils.writeValueAsString(oldTags);
        SettingEntity settingEntity = SettingEntity.builder()
                .settingName(AppUtils.SettingName.MODULE_TYPE.getSetting()) // Use exact enum value
                .settingValue(oldTagsJson)
                .build();
        when(settingRepository.findBySettingName(AppUtils.SettingName.MODULE_TYPE.getSetting()))
                .thenReturn(Optional.of(settingEntity));
        Set<String> newTags = new HashSet<>(Set.of("tag3"));
        settingService.updateSetting(AppUtils.SettingName.MODULE_TYPE, newTags);
        Set<String> expectedTags = new HashSet<>(Set.of("tag1", "tag2", "tag3"));
        Set<String> actualTags = AppUtils.readValue(
                settingEntity.getSettingValue(),
                new TypeReference<>() {
                }
        );
        assertEquals(expectedTags, actualTags);
        verify(settingRepository, times(1)).save(settingEntity);
    }
    @Test
    void testUpdateSettingSettingNotFound() {
        when(settingRepository.findBySettingName(AppUtils.SettingName.MODULE_TYPE.getSetting()))
                .thenReturn(Optional.empty());
        Set<String> newTags = new HashSet<>(Set.of("tag3"));
        Exception exception = assertThrows(
                RuntimeException.class,
                () -> settingService.updateSetting(AppUtils.SettingName.MODULE_TYPE, newTags)
        );
        String expectedMessage = AppUtils.Messages.SETTING_NOT_FOUND.getMessage();
        assertTrue(exception.getMessage().contains(expectedMessage));
        verify(settingRepository, never()).save(any());
    }
    @Test
    void testUpdateSync() {
        SettingEntity settingEntity = SettingEntity.builder()
                .settingName("STORAGE_SYNC")
                .settingValue("oldTime")
                .build();
        when(settingRepository.findBySettingName("STORAGE_SYNC")).thenReturn(Optional.of(settingEntity));
        settingService.updateSync("STORAGE_SYNC", "2025-09-27T10:00:00");
        assertEquals("2025-09-27T10:00:00", settingEntity.getSettingValue());
        verify(settingRepository, times(1)).save(settingEntity);
    }
}
