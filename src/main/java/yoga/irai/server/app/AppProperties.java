package yoga.irai.server.app;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String e2eOtp;
    private String e2eUserId;
    private String defaultUserId;
    private String settingToSkip;
    private String mediaExtensions;
    private String defaultOrganizationId;
}
