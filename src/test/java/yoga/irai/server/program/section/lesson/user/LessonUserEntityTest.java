package yoga.irai.server.program.section.lesson.user;

import org.junit.jupiter.api.Test;
import yoga.irai.server.app.AppUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LessonUserEntityTest {
    @Test
    void prePersist_ShouldSetDefaultStatusToStarted() {
        LessonUserEntity entity = LessonUserEntity.builder()
                .lessonUserId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .lessonId(UUID.randomUUID())
                .resumeTime(120L)
                .build();
        entity.onCreate();
        assertEquals(AppUtils.LessonUserStatus.STARTED, entity.getLessonUserStatus());
    }
}
