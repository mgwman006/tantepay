package pay.tante.tz.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
  private final String[] allowedOrigins;

  public CorsConfig(
    @Value("${tantepay.cors.allowed-origins:http://localhost:4179}") String allowedOrigins
  ) {
    this.allowedOrigins = allowedOrigins.split(",");
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/v1/**")
      .allowedOrigins(allowedOrigins)
      .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
      .allowedHeaders("Authorization", "Content-Type", "Accept", "Origin")
      .allowCredentials(true);
  }
}
