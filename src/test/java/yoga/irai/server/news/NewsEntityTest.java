package yoga.irai.server.news;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import yoga.irai.server.app.AppUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class NewsEntityTest {
    @Test
    void testOnCreate_ShouldSetNewsStatusToInactive() {
        NewsEntity entity = new NewsEntity();
        entity.onCreate();
        assertEquals(AppUtils.NewsStatus.ACTIVE, entity.getNewsStatus());
    }
}
