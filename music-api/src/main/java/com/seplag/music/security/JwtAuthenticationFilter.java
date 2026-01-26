package com.seplag.music.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // LOG ESTRATÉGICO: Verifique no seu terminal o que aparece aqui!
        log.info("JWT Filter interceptando: {} {}", request.getMethod(), path);

        // Verificação mais robusta para endpoints públicos
        boolean isPublicEndpoint = path.contains("/api/v1/auth/register") ||
                                   path.contains("/api/v1/auth/login") ||
                                   path.contains("/api/v1/auth/refresh") ||
                                   path.contains("/swagger") ||
                                   path.contains("/v3/api-docs") ||
                                   path.contains("/actuator");

        if (isPublicEndpoint) {
            log.info("Endpoint público detectado, ignorando validação JWT para: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);

            if (jwt != null && jwtProvider.validateToken(jwt)) {
                String username = jwtProvider.getUsernameFromToken(jwt);
                String role = jwtProvider.getRoleFromToken(jwt);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Usuário autenticado via JWT: {}", username);
            }
        } catch (Exception ex) {
            log.error("Erro ao processar JWT para o caminho {}: {}", path, ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}