package yoga.irai.server.event;

import org.junit.jupiter.api.Test;
import yoga.irai.server.app.AppUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventEntityTest {

    @Test
    void testOnCreate_ShouldSetEventStatusToInactive() {
        EventEntity entity = new EventEntity();
        entity.onCreate();
        assertEquals(AppUtils.EventStatus.INACTIVE, entity.getEventStatus());
    }
}
