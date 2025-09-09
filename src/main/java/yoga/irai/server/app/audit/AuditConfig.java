package yoga.irai.server.app.audit;

import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import yoga.irai.server.app.AppUtils;

@Configuration
public class AuditConfig {

    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> Optional.of(AppUtils.getPrincipalUserId());
    }
}
