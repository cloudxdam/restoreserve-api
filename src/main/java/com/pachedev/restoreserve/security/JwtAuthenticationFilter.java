package com.pachedev.restoreserve.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Filtro que intercepta todas las peticiones HTTP para validar el token JWT.
 * Si el token es correcto, identica al usuario y le da acceso a la aplicación.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Proceso de filtrado principal:
     * 1. extrae el token del encabezado Authorization.
     * 2. valida la firma y la fecha del token.
     * 3. registra al usuario en la lista (contexto) de seguridad.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // extraer el encabezado Authorization
        String authHeader = request.getHeader("Authorization");

        // si la solicitud no trae token JWT o no empieza por Bearer pasamos la petición
        // al siguiente filtro
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // extraer el token (7 caracteres-> Bearer + el espacio que precede al token)
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        // si hay un token y no está ya autenticado, buscamos los datos del usuario en
        // la base de datos
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            // Si el token es válido (firmado y no expirado) creamos la credencial de acceso
            // para Spring Security
            if (jwtService.isTokenValid(token, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // autorizamos al usuario en el sistema
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        // continuamos con la cadena de filtros de Spring Security
        filterChain.doFilter(request, response);
    }

}
