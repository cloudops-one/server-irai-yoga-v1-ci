package yoga.irai.server.app.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        JwtFilterConfig jwtFilterConfig = Mockito.mock(JwtFilterConfig.class);
        securityConfig = new SecurityConfig(jwtFilterConfig);
    }

    @Test
    void testFilterChainBean() {
        HttpSecurity httpSecurity = Mockito.mock(HttpSecurity.class, Mockito.RETURNS_DEEP_STUBS);
        assertDoesNotThrow(() -> securityConfig.filterChain(httpSecurity));
    }

    @Test
    void testAuthenticationManagerBean() throws Exception {
        AuthenticationConfiguration config = Mockito.mock(AuthenticationConfiguration.class);
        AuthenticationManager manager = Mockito.mock(AuthenticationManager.class);
        Mockito.when(config.getAuthenticationManager()).thenReturn(manager);

        AuthenticationManager result = securityConfig.authenticationManager(config);
        assertNotNull(result);
        assertEquals(manager, result);
    }
}