package yoga.irai.server.shorts.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import yoga.irai.server.app.AppUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ShortsUserEntityTest {
    @Test
    void onCreateTest() {
        ShortsUserEntity entity = new ShortsUserEntity();
        entity.onCreate();
        assertEquals(AppUtils.ShortsUserStatus.NEW, entity.getShortsUserStatus());
    }
}
