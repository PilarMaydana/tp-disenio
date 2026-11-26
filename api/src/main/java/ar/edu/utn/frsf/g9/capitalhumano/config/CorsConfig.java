package ar.edu.utn.frsf.g9.capitalhumano.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración global de CORS.
 *
 * <p>Permite que el frontend (Next.js en {@code localhost:3000})
 * consuma la API sin restricciones de origen cruzado.</p>
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    /**
     * Registra las reglas de CORS para todos los endpoints de la API.
     *
     * @return configurador de CORS
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        // Permite al frontend leer el nombre del archivo Excel (CU 025)
                        .exposedHeaders("Content-Disposition")
                        .allowCredentials(true);
            }
        };
    }
}
