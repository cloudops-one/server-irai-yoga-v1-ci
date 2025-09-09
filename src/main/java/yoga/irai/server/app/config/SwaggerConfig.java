package yoga.irai.server.app.config;

import static yoga.irai.server.app.AppUtils.Constants;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
     * Configures OpenAPI documentation for the Irai Yoga application.
     *
     * @return OpenAPI instance with custom configuration.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Irai Yoga API").description("API documentation for Irai Yoga application")
                        .version("v1.0")
                        .contact(new Contact().name("Irai Yoga Team").email("support@terv.pro")
                                .url("https://irai.yoga")))
                .components(new Components().addSecuritySchemes(Constants.SECURITY_SCHEME_NAME,
                        new SecurityScheme().name(Constants.SECURITY_SCHEME_NAME).type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement()
                        .addList(Constants.SECURITY_SCHEME_NAME))
                .externalDocs(new ExternalDocumentation().description("""
                        Irai Yoga Documentation

                        Api Responses:
                        - 200: OK
                        - 400: Bad Request
                        - 401: Unauthorized
                        - 403: Forbidden
                        - 404: Not Found
                        - 500: Internal Server Error
                        """).url("https://irai.yoga/docs"));
    }

    /**
     * Configures a grouped OpenAPI instance for public APIs.
     *
     * @return GroupedOpenApi instance for public APIs.
     */
    @Bean
    public GroupedOpenApi portalApi() {
        return GroupedOpenApi.builder().group("portal-api")
                .pathsToMatch("/auth/**", "/dashboard/portal", "/events/**", "/news/**", "/organization/**",
                        "/poems/**", "/practice/**", "/program/**", "/setting/**", "/shorts/**", "/storage/**",
                        "/users/**", "/enquiry/**", "/notification/**")
                .build();
    }

    @Bean
    public GroupedOpenApi mobileApi() {
        return GroupedOpenApi.builder().group("mobile-api").pathsToMatch("/auth/**", "/mobile/**").build();
    }
}
