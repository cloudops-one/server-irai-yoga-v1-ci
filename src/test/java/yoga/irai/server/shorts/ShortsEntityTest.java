package yoga.irai.server.shorts;

import org.junit.jupiter.api.Test;
import yoga.irai.server.app.AppUtils;

import static org.junit.jupiter.api.Assertions.*;

class ShortsEntityTest {
    @Test
    void onCreateTest() {
        ShortsEntity entity = ShortsEntity.builder()
                .shortsName("Test Shorts")
                .shortsDescription("Test 1")
                .build();
        assertNull(entity.getShortsStatus());
        entity.onCreate();
        assertEquals(AppUtils.ShortsStatus.INACTIVE, entity.getShortsStatus());
    }
}
