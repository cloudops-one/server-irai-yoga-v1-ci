package yoga.irai.server.setting;

import org.junit.jupiter.api.Test;
import yoga.irai.server.app.AppUtils;


import static org.junit.jupiter.api.Assertions.assertEquals;

class SettingEntityTest {
    @Test
    void testPrePersistSetsStatusToInactive() {
        SettingEntity entity = new SettingEntity();
        entity.setSettingStatus(AppUtils.SettingStatus.ACTIVE);
        entity.onCreate();
        assertEquals(AppUtils.SettingStatus.INACTIVE, entity.getSettingStatus());
    }
}
