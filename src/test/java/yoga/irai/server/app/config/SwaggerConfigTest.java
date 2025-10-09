package yoga.irai.server.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

import static org.junit.jupiter.api.Assertions.*;

class SwaggerConfigTest {

    private SwaggerConfig swaggerConfig;

    @BeforeEach
    void setUp() {
        swaggerConfig = new SwaggerConfig();
    }

    @Test
    void testCustomOpenAPIBean() {
        OpenAPI openAPI = swaggerConfig.customOpenAPI();
        assertNotNull(openAPI);
        Info info = openAPI.getInfo();
        assertNotNull(info);
        assertEquals("Irai Yoga API", info.getTitle());
        assertEquals("v1.0", info.getVersion());
    }

    @Test
    void testPortalApiBean() {
        GroupedOpenApi portalApi = swaggerConfig.portalApi();
        assertNotNull(portalApi);
        assertEquals("portal-api", portalApi.getGroup());
    }

    @Test
    void testMobileApiBean() {
        GroupedOpenApi mobileApi = swaggerConfig.mobileApi();
        assertNotNull(mobileApi);
        assertEquals("mobile-api", mobileApi.getGroup());
    }
}