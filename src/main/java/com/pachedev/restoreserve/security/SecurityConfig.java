package com.pachedev.restoreserve.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Configuración de seguridad de la API:
 * - Define cómo se protegen los datos (hash).
 * - Configura la sesión como stateless mediante JWT.
 * - Declara qué usuarios pueden acceder los endpoints. * 
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * sirve para hashear contraseñas (hacerlas ilegibles) y verificar que la
     * contraseña que introduce el usuario coincide con el hash guardado, sin
     * necesidad de saber cual era la contraseña original
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Cadena de filtros de Seguridad de la API, los controles automáticos por donde
     * pasará cada petición HTTP:
     * 
     * 1. Desactiva CSRF (no la necesitamos, nuestra API es stateless gracias al
     * uso de tokens JWT).
     * 2. definimos que la política de sesión es stateless (no guardará la
     * sesión en el servidor, cada petición debe traer credenciales).
     * 3. declaramos los endpoints a lo que todo el mundo pueda acceder (permitAll).
     * 4. declaramos los endpoints a los que sólo puede acceder usuario con rol
     * ADMIN.
     * 5. declaramos los endpoints a los que pueden acceder los que tengan rol USER
     * o ADMIN.
     * 6. anyRequest().authenticated() .> cualquier otra ruta requiere usuario
     * autenticado.
     * 7. addFilterBefore -> antes de pasar el filtro de Spring Security debe pasar
     * el que hemos definido nosotros, que leerá el token y autenticará al usuario
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth -> auth.requestMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll()
                                .requestMatchers("/api/v1/tables/**").hasRole("ADMIN")
                                .requestMatchers("/api/v1/reservations/**").hasAnyRole("USER", "ADMIN")
                                .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
