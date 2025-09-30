package yoga.irai.server.practice.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import yoga.irai.server.app.AppUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PracticeUserEntityTest {
    @Test
    void testOnCreate_ShouldSetPracticeUserStatusToInactive() {
        PracticeUserEntity entity = new PracticeUserEntity();
        entity.onCreate();
        assertEquals(AppUtils.PracticeUserStatus.STARTED, entity.getPracticeUserStatus());
    }
}
