package co.edu.uniquindio.backendpawsoft.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro encargado de interceptar cada petición HTTP
 * y validar el token JWT enviado en el header Authorization.
 *
 * Este filtro:
 * - Verifica si la petición contiene un token Bearer.
 * - Extrae el username (email) desde el JWT.
 * - Valida la firma y expiración del token.
 * - Si el token es válido, autentica al usuario en el contexto de Spring Security.
 *
 * En caso de que el token sea inválido, esté expirado o tenga una firma incorrecta,
 * el filtro no interrumpe la ejecución, simplemente continúa la cadena de filtros,
 * permitiendo que Spring Security maneje el acceso correspondiente.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Materia: Software III
 *
 * Autoras:
 * - Valentina Porras Salazar
 * - Helen Xiomara Giraldo Libreros
 *
 * Profesor:
 * Raúl Yulbraynner Rivera Gálvez
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Método principal que se ejecuta por cada petición HTTP.
     *
     * @param request  petición HTTP entrante
     * @param response respuesta HTTP
     * @param filterChain cadena de filtros de Spring Security
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Si no existe el header Authorization o no es tipo Bearer,
        // se continúa con la cadena de filtros sin autenticar.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {

            // Extrae el email (username) desde el token
            final String userEmail = jwtService.extractUsername(jwt);

            // Verifica que no exista autenticación previa en el contexto
            if (userEmail != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                // Carga los detalles del usuario desde la base de datos
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(userEmail);

                // Valida el token (firma y expiración)
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // Crea el objeto de autenticación
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    // Establece la autenticación en el contexto de seguridad
                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);
                }
            }

        } catch (Exception e) {
            /*
             * Si ocurre cualquier excepción (firma inválida, token expirado,
             * token mal formado, etc.), simplemente no se autentica el usuario.
             * No se interrumpe la ejecución para permitir que Spring Security
             * maneje adecuadamente el acceso al recurso solicitado.
             */
        }

        filterChain.doFilter(request, response);
    }
}