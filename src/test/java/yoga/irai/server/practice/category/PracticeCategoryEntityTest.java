package yoga.irai.server.practice.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import yoga.irai.server.app.AppUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PracticeCategoryEntityTest {
    @Test
    void testOnCreate_ShouldSetPracticeCategoryStatusToInactive() {
        PracticeCategoryEntity entity = new PracticeCategoryEntity();
        entity.onCreate();
        assertEquals(AppUtils.PracticeCategoryStatus.INACTIVE, entity.getPracticeCategoryStatus());
    }

}
