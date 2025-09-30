package yoga.irai.server.practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import yoga.irai.server.app.AppUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PracticeEntityTest {
    @Test
    void testOnCreate_ShouldSetPracticeStatusToInactive() {
        PracticeEntity entity = new PracticeEntity();
        entity.onCreate();
        assertEquals(AppUtils.PracticeStatus.INACTIVE, entity.getPracticeStatus());
    }
}
