package yoga.irai.server.app.validator;

import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import yoga.irai.server.app.AppProperties;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.setting.SettingService;

@Slf4j
@Component
@AllArgsConstructor
public class SettingValidator implements CommandLineRunner {
    private final AppProperties appProperties;
    private final SettingService settingService;

    @Override
    public void run(String... args) {
        for (AppUtils.SettingName setting : AppUtils.SettingName.values()) {
            String settingName = setting.getSetting();
            if (appProperties.getSettingToSkip().contains(settingName)) {
                continue;
            }
            try {
                Class<?> settingClass = Class.forName(AppUtils.Constants.SETTING_PACKAGE + settingName);
                if (!settingClass.isEnum()) {
                    log.error(AppUtils.Messages.CLASS_NOT_ENUM.getMessage(settingName));
                    continue;
                }
                @SuppressWarnings("unchecked")
                Set<String> settingKeys = Arrays.stream(((Class<? extends Enum<?>>) settingClass).getEnumConstants())
                        .map(Enum::name).collect(Collectors.toSet());

                String settingValue = settingService.getSettingBySettingName(settingName).getSettingValue();
                if (settingValue.isBlank()) {
                    log.error(AppUtils.Messages.SETTING_MISMATCH.getMessage(settingName, settingKeys));
                    System.exit(0);
                }
                JSONArray jsonArray = new JSONArray(settingValue);
                Set<String> dbKeys = new HashSet<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    dbKeys.add(obj.getString("key"));
                }
                if (settingKeys.equals(dbKeys)) {
                    log.info(AppUtils.Messages.SETTING_VALIDATED.getMessage(settingName));
                } else {
                    log.error(AppUtils.Messages.SETTING_MISMATCH.getMessage(settingName, settingKeys, dbKeys));
                    System.exit(0);
                }
            } catch (ClassNotFoundException e) {
                log.error(AppUtils.Messages.SETTING_ENUM_NOT_FOUND.getMessage(settingName));
                System.exit(0);
            }
        }
    }
}
