package co.edu.uniquindio.backendpawsoft.security;

/**
 * Filtro de autenticación JWT para interceptar y validar tokens en las peticiones HTTP.
 * Extrae el token del header Authorization y establece el contexto de seguridad de Spring.
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
 * - Raúl Yulbraynner Rivera Gálvez
 */
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro encargado de interceptar cada petición HTTP y validar el token JWT enviado
 * en el header Authorization.
 *
 * Responsabilidades:
 * - Verificar si la petición contiene un token con prefijo {@code Bearer }.
 * - Extraer el username (email) y el rol desde el JWT.
 * - Validar la firma y la expiración del token.
 * - Si el token es válido, autenticar al usuario en el contexto de Spring Security.
 *
 * Comportamiento ante errores:
 * - Si el token no existe o no inicia con Bearer, el filtro no autentica y continúa.
 * - Si ocurre cualquier excepción (token inválido, expirado o mal formado), el filtro
 *   no autentica al usuario y permite que Spring Security gestione el acceso según las reglas.
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

    /**
     * Servicio encargado de extraer información y validar tokens JWT.
     */
    private final JwtService jwtService;

    /**
     * Servicio de Spring Security para cargar usuarios a partir del username (email).
     */
    private final UserDetailsService userDetailsService;


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getServletPath();

        return path.startsWith("/auth/password-reset")
                || path.startsWith("/auth/login")
                || path.startsWith("/auth/register")
                || path.startsWith("/auth/verify-email")
                || path.startsWith("/auth/resend-verification")
                || path.startsWith("/auth/refresh")
                || path.startsWith("/auth/logout");
    }



    /**
     * Método que se ejecuta en cada petición HTTP para intentar autenticar al usuario
     * con base en el token JWT recibido.
     *
     * Si el token es válido, se registra la autenticación en el {@link SecurityContextHolder}.
     *
     * @param request petición HTTP entrante
     * @param response respuesta HTTP
     * @param filterChain cadena de filtros de Spring Security
     * @throws ServletException si ocurre un error del tipo servlet
     * @throws IOException si ocurre un error de entrada/salida
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {

                    String role = jwtService.extractRole(jwt);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    List.of(new SimpleGrantedAuthority(role))
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

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