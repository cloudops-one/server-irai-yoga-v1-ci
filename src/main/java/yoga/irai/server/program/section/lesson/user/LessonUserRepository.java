package yoga.irai.server.program.section.lesson.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonUserRepository extends JpaRepository<LessonUserEntity, UUID> {
    LessonUserEntity getByLessonIdAndUserId(UUID lessonId, UUID userId);

    Optional<LessonUserEntity> findByLessonIdAndUserId(UUID lessonId, UUID userId);
}
