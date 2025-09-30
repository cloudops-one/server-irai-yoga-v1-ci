package yoga.irai.server.poem;

import org.junit.jupiter.api.Test;
import yoga.irai.server.app.AppUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PoemEntityTest {
    @Test
    void testOnCreate_ShouldSetPoemStatusToInactive() {

        PoemEntity entity = new PoemEntity();
        entity.onCreate();
        assertEquals(AppUtils.PoemStatus.INACTIVE, entity.getPoemStatus());
    }
}
