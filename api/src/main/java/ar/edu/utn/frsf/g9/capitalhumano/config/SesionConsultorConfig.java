package ar.edu.utn.frsf.g9.capitalhumano.config;

import ar.edu.utn.frsf.g9.capitalhumano.features.auth.service.SesionConsultorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Exige sesión de consultor en los endpoints del CU 025 - Evaluar a candidatos.
 *
 * <p>Esos endpoints exponen datos personales de los candidatos y sus claves de ingreso, por
 * lo que no pueden quedar abiertos. Si no hay sesión de consultor se responde 401 con el
 * formato {@code ApiError} (lo resuelve {@code GlobalExceptionHandler}).</p>
 *
 * <p>Las solicitudes preflight de CORS no llevan el token, por eso se dejan pasar.</p>
 */
@Configuration
public class SesionConsultorConfig implements WebMvcConfigurer {

    /** Rutas exclusivas del consultor (CU 025). */
    static final String[] RUTAS_CONSULTOR = {"/api/candidatos/**", "/api/evaluaciones/**"};

    private final SesionConsultorService sesionConsultorService;

    public SesionConsultorConfig(SesionConsultorService sesionConsultorService) {
        this.sesionConsultorService = sesionConsultorService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                if (!CorsUtils.isPreFlightRequest(request)) {
                    sesionConsultorService.verificarConsultor(request.getHeader(HttpHeaders.AUTHORIZATION));
                }
                return true;
            }
        }).addPathPatterns(RUTAS_CONSULTOR);
    }
}
