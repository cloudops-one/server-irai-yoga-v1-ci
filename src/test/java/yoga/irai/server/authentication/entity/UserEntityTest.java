package yoga.irai.server.authentication.entity;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import yoga.irai.server.app.AppUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class UserEntityTest {
    @Test
    void onCreateTest() {
        UUID userId = UUID.randomUUID();
        UserEntity entity = new UserEntity();
        entity.setOrgId(UUID.randomUUID());
        entity.setUserIconStorageId(UUID.randomUUID());

        try (MockedStatic<AppUtils> mocked = mockStatic(AppUtils.class)) {
            mocked.when(AppUtils::getPrincipalUserId).thenReturn(userId);

            entity.onCreate();

            assertEquals(userId, entity.getCreatedBy());
            assertEquals(userId, entity.getUpdatedBy());
            assertFalse(entity.isEmailVerified());
            assertFalse(entity.isMobileVerified());
        }
    }
    @Test
    void onUpdateTest() {
        UUID fakeUserId = UUID.randomUUID();
        UserEntity entity = new UserEntity();

        try (MockedStatic<AppUtils> mocked = mockStatic(AppUtils.class)) {
            mocked.when(AppUtils::getPrincipalUserId).thenReturn(fakeUserId);

            entity.onUpdate();

            assertEquals(fakeUserId, entity.getUpdatedBy());
        }
    }
    @Test
    void onCreateFailureTest(){
        UserEntity entity = new UserEntity();
        entity.setSkipAudit(true);
        entity.setOrgId(UUID.randomUUID());
        entity.onCreate();
        assertNull(entity.getCreatedBy());
    }
    @Test
    void onUpdatFailureTest(){
        UserEntity entity = new UserEntity();
        entity.setSkipAudit(true);
        entity.setOrgId(UUID.randomUUID());
        entity.onUpdate();
        assertNull(entity.getUpdatedBy());
    }
}
