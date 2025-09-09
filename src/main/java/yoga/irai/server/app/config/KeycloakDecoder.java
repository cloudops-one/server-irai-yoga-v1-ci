package yoga.irai.server.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;

@Configuration
public class KeycloakDecoder {

    @Value("${spring.security.oauth2.client.provider.irai-yoga-v1-service-account-client.issuer-uri}")
    private String issuerUri;

    @Bean
    public JwtDecoder keycloakJwtDecoder() {
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }
}
