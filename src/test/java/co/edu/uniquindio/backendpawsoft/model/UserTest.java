package co.edu.uniquindio.backendpawsoft.model;

import co.edu.uniquindio.backendpawsoft.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el modelo de Usuario.
 * 
 * Valida la correcta implementación de UserDetails y el comportamiento
 * de los métodos relacionados con la seguridad de Spring Security.
 */
@DisplayName("Pruebas del Modelo User")
class UserTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Usuario Test")
                .email("test@example.com")
                .phone("3001234567")
                .password("$2a$10$hashedPassword")
                .role(Role.ROLE_CLIENTE)
                .enabled(true)
                .primerAcceso(false)
                .failedAttempts(0)
                .photoUrl("http://example.com/photo.jpg")
                .build();
    }

    @Test
    @DisplayName("Usuario debe implementar UserDetails correctamente")
    void testUserDetailsImplementation() {
        // Assert
        assertEquals("test@example.com", testUser.getUsername());
        assertEquals("$2a$10$hashedPassword", testUser.getPassword());
        assertTrue(testUser.isEnabled());
        assertTrue(testUser.isAccountNonExpired());
        assertTrue(testUser.isAccountNonLocked());
        assertTrue(testUser.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("Authorities debe contener el rol del usuario")
    void testGetAuthorities() {
        // Act
        Collection<? extends GrantedAuthority> authorities = testUser.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_CLIENTE")));
    }

    @Test
    @DisplayName("Usuario con diferentes roles debe retornar authority correcto")
    void testDiferentesRoles() {
        // Arrange
        User veterinario = User.builder()
                .role(Role.ROLE_VETERINARIO)
                .build();

        User recepcionista = User.builder()
                .role(Role.ROLE_RECEPCIONISTA)
                .build();

        User admin = User.builder()
                .role(Role.ROLE_ADMIN)
                .build();

        // Assert
        assertEquals("ROLE_VETERINARIO", 
                veterinario.getAuthorities().iterator().next().getAuthority());
        assertEquals("ROLE_RECEPCIONISTA", 
                recepcionista.getAuthorities().iterator().next().getAuthority());
        assertEquals("ROLE_ADMIN", 
                admin.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    @DisplayName("Usuario deshabilitado debe retornar isEnabled false")
    void testUsuarioDeshabilitado() {
        // Arrange
        testUser.setEnabled(false);

        // Assert
        assertFalse(testUser.isEnabled());
    }

    @Test
    @DisplayName("Usuario con lockTime debe considerarse bloqueado")
    void testUsuarioBloqueado() {
        // Arrange
        testUser.setLockTime(LocalDateTime.now());

        // Assert
        assertNotNull(testUser.getLockTime());
    }

    @Test
    @DisplayName("Incrementar intentos fallidos debe actualizar contador")
    void testIncrementarIntentosFallidos() {
        // Act
        testUser.setFailedAttempts(testUser.getFailedAttempts() + 1);

        // Assert
        assertEquals(1, testUser.getFailedAttempts());
    }

    @Test
    @DisplayName("Usuario en primer acceso debe tener flag correcto")
    void testPrimerAcceso() {
        // Arrange
        User nuevoUsuario = User.builder()
                .primerAcceso(true)
                .build();

        // Assert
        assertTrue(nuevoUsuario.isPrimerAcceso());
        assertFalse(testUser.isPrimerAcceso());
    }

    @Test
    @DisplayName("Builder debe crear usuario con todos los campos")
    void testBuilderCompleto() {
        // Arrange & Act
        User usuario = User.builder()
                .id(2L)
                .name("Nuevo Usuario")
                .email("nuevo@example.com")
                .phone("3009876543")
                .password("password")
                .role(Role.ROLE_VETERINARIO)
                .enabled(true)
                .primerAcceso(true)
                .failedAttempts(0)
                .lockTime(null)
                .photoUrl("http://example.com/photo.jpg")
                .build();

        // Assert
        assertEquals(2L, usuario.getId());
        assertEquals("Nuevo Usuario", usuario.getName());
        assertEquals("nuevo@example.com", usuario.getEmail());
        assertEquals("3009876543", usuario.getPhone());
        assertEquals(Role.ROLE_VETERINARIO, usuario.getRole());
        assertTrue(usuario.isEnabled());
        assertTrue(usuario.isPrimerAcceso());
    }
}
