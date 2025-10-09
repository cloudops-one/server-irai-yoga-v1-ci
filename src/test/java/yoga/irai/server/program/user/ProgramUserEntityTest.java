package yoga.irai.server.program.user;

import org.junit.jupiter.api.Test;
import yoga.irai.server.app.AppUtils;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ProgramUserEntityTest {
    @Test
    void onCreateTest() {
        ProgramUserEntity entity = ProgramUserEntity.builder()
                .programUserId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .programId(UUID.randomUUID())
                .build();
        entity.onCreate();
        assertThat(entity.getProgramUserStatus()).isEqualTo(AppUtils.ProgramUserStatus.STARTED);
    }
}
