package yoga.irai.server.storage;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import yoga.irai.server.app.AppUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StorageEntityTest {
    @Test
    void onCreateTest() {
        UUID userId = UUID.randomUUID();

        try (MockedStatic<AppUtils> mockedAppUtils = Mockito.mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserId).thenReturn(userId);

            StorageEntity storageEntity = new StorageEntity();
            storageEntity.onCreate();

            assertEquals(userId, storageEntity.getCreatedBy());
        }
    }
}
