package ar.edu.utn.frsf.g9.capitalhumano.config;

import ar.edu.utn.frsf.g9.capitalhumano.features.auth.service.JwtTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtTokenService jwtTokenService) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
                        .permitAll()
                        .requestMatchers("/api/auth/**", "/api/ping").permitAll()
                        .requestMatchers("/api/cuestionarios/**").hasRole("CANDIDATO")
                        .requestMatchers("/api/**").hasRole("CONSULTOR")
                        .anyRequest().denyAll())
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenService), UsernamePasswordAuthenticationFilter.class);
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        // TODO CU 01: exigir autenticación
                        .requestMatchers("/api/competencias/**").permitAll()
                        .requestMatchers("/api/puestos/**").permitAll()
                        // TODO CU 01: exigir autenticación
                        .requestMatchers("/api/factores/**").permitAll()
                        // TODO CU 01: exigir autenticación
                        .requestMatchers("/api/preguntas/**").permitAll()
                        // TODO CU 01: exigir autenticación
                        .requestMatchers("/api/opciones-respuesta/**").permitAll()
                        // TODO CU 01: exigir autenticación de consultor (CU 025)
                        .requestMatchers("/api/candidatos/**").permitAll()
                        .requestMatchers("/api/evaluaciones/**").permitAll()
                        // CU 026: el candidato se valida con su token dentro del controlador
                        .requestMatchers("/api/cuestionarios/**").permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
