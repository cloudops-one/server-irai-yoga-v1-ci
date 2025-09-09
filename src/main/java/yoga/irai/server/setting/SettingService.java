package yoga.irai.server.setting;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import yoga.irai.server.app.AppUtils;

/**
 * Service class for managing settings. Provides methods to add, update, delete,
 * and retrieve settings.
 */
@Service
@AllArgsConstructor
public class SettingService {

    private final SettingRepository settingRepository;

    /**
     * @param settingName
     *            the name of the setting to retrieve
     * @return the SettingEntity with the specified name
     */
    public SettingEntity getSettingBySettingName(String settingName) {
        return settingRepository.findBySettingName(settingName)
                .orElseThrow(AppUtils.Messages.SETTING_NOT_FOUND::getException);
    }

    /**
     * Updates the tags of a setting by its name. It retrieves the existing
     *
     * @param settingName
     *            - the name of the setting to update
     */
    public void updateSetting(AppUtils.SettingName settingName, Set<String> tags) {
        SettingEntity settingEntity = getSettingBySettingName(settingName.getSetting());
        Set<String> oldTags = AppUtils.readValue(settingEntity.getSettingValue(), new TypeReference<>() {
        });
        if (ObjectUtils.isNotEmpty(oldTags)) {
            tags.addAll(oldTags);
        }
        settingEntity.setSettingValue(AppUtils.writeValueAsString(tags));
        settingRepository.save(settingEntity);
    }

    public void updateSync(String storageSyncedAt, String syncedTime) {
        SettingEntity settingEntity = getSettingBySettingName(storageSyncedAt);
        settingEntity.setSettingValue(syncedTime);
        settingRepository.save(settingEntity);
    }
}
