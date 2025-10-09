package yoga.irai.server.app.audit;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import yoga.irai.server.app.AppUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuditConfigTest {

    @Test
    void testAuditorProviderReturnsPrincipalUserId() {
        UUID expectedId = UUID.randomUUID();
        try (MockedStatic<AppUtils> utils = org.mockito.Mockito.mockStatic(AppUtils.class)) {
            utils.when(AppUtils::getPrincipalUserId).thenReturn(expectedId);

            AuditConfig config = new AuditConfig();
            Optional<UUID> auditor = config.auditorProvider().getCurrentAuditor();
            assertTrue(auditor.isPresent());
            assertEquals(expectedId, auditor.get());
        }
    }
}