package pay.tante.tz.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public GroupedOpenApi paymentApi() {
    return GroupedOpenApi.builder()
      .group("payment")
      .packagesToScan("pay.tante.tz.controllers")
      .build();
  }

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
      .info(new Info()
        .title("Tantepay Payment API")
        .description("Payment processing service for Tante platform")
        .version("1.0.0"))
      .addSecurityItem(new SecurityRequirement().addList("Bearer"))
      .components(new Components()
        .addSecuritySchemes("Bearer",
          new SecurityScheme()
            .name("Bearer")
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
        )
      );
  }
}
