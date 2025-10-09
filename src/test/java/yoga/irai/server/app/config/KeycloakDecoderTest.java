package yoga.irai.server.app.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class KeycloakDecoderTest {

    private KeycloakDecoder keycloakDecoder;

    @BeforeEach
    void setUp() throws Exception {
        keycloakDecoder = new KeycloakDecoder();
        Field issuerUriField = KeycloakDecoder.class.getDeclaredField("issuerUri");
        issuerUriField.setAccessible(true);
        issuerUriField.set(keycloakDecoder, "https://keycloak.cloudops.terv.pro/auth/realms/terv-pro-realm");
    }

    @Test
    void testKeycloakJwtDecoderBean() {
        JwtDecoder decoder = keycloakDecoder.keycloakJwtDecoder();
        assertNotNull(decoder);
    }
}