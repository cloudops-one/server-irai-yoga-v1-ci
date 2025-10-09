package yoga.irai.server.program;

import org.junit.jupiter.api.Test;
import yoga.irai.server.app.AppUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ProgramEntityTest {
    @Test
    void onCreate_ShouldSetProgramStatusToInactive() {
        ProgramEntity entity = ProgramEntity.builder()
                .programName("Test Program")
                .build();
        entity.onCreate();
        assertThat(entity.getProgramStatus()).isEqualTo(AppUtils.ProgramStatus.INACTIVE);
        assertThat(entity.getDuration()).isZero();
        assertThat(entity.getNumberOfLessons()).isZero();
    }
}
