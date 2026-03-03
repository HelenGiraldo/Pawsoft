package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.UserRequest;
import co.edu.uniquindio.backendpawsoft.dto.UserResponse;
import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.EmailVerificationTokenRepository;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el servicio de gestión de usuarios.
 * 
 * Valida las operaciones CRUD sobre usuarios, incluyendo:
 * - Creación de usuarios clientes
 * - Creación de usuarios staff con contraseña temporal
 * - Actualización y eliminación de usuarios
 * - Validación de reglas de negocio (correos únicos, contraseñas fuertes)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del Servicio de Usuarios")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRequest userRequest;

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
                .build();

        userRequest = new UserRequest();
        userRequest.setName("Nuevo Usuario");
        userRequest.setEmail("nuevo@example.com");
        userRequest.setPhone("3009876543");
        userRequest.setPassword("Password123!");
    }

    @Test
    @DisplayName("Obtener todos los usuarios debe retornar lista completa")
    void testGetAllUsers() {
        // Arrange
        User user2 = User.builder()
                .id(2L)
                .name("Usuario 2")
                .email("user2@example.com")
                .role(Role.ROLE_VETERINARIO)
                .enabled(true)
                .build();
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        // Act
        List<UserResponse> users = userService.getAllUsers();

        // Assert
        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals("Usuario Test", users.get(0).getName());
        assertEquals("Usuario 2", users.get(1).getName());
    }

    @Test
    @DisplayName("Obtener usuario por ID debe retornar usuario correcto")
    void testGetUserById() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        UserResponse response = userService.getUserById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getName(), response.getName());
        assertEquals(testUser.getEmail(), response.getEmail());
    }

    @Test
    @DisplayName("Obtener usuario inexistente debe lanzar excepción")
    void testGetUserByIdNoExiste() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.getUserById(999L));
    }

    @Test
    @DisplayName("Crear usuario con correo único debe ser exitoso")
    void testCreateUserExitoso() {
        // Arrange
        when(userRepository.findByEmail(userRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userRequest.getPassword())).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(emailVerificationTokenRepository.save(any())).thenReturn(null);

        // Act
        UserResponse response = userService.createUser(userRequest);

        // Assert
        assertNotNull(response);
        assertEquals(userRequest.getName(), response.getName());
        assertEquals(userRequest.getEmail(), response.getEmail());
        verify(emailService, times(1)).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Crear usuario con correo duplicado debe lanzar excepción")
    void testCreateUserCorreoDuplicado() {
        // Arrange
        when(userRepository.findByEmail(userRequest.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(userRequest)
        );
        assertTrue(exception.getMessage().contains("ya está registrado"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Actualizar usuario debe modificar datos correctamente")
    void testUpdateUser() {
        // Arrange
        UserRequest updateRequest = new UserRequest();
        updateRequest.setName("Nombre Actualizado");
        updateRequest.setEmail("test@example.com");
        updateRequest.setPhone("3001111111");
        updateRequest.setPassword("NewPassword123!");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(updateRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(updateRequest.getPassword())).thenReturn("$2a$10$newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse response = userService.updateUser(1L, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Nombre Actualizado", testUser.getName());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Eliminar usuario existente debe ser exitoso")
    void testDeleteUser() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar usuario inexistente debe lanzar excepción")
    void testDeleteUserNoExiste() {
        // Arrange
        when(userRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.deleteUser(999L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Crear usuario staff debe generar contraseña temporal")
    void testCreateStaffUser() {
        // Arrange
        when(userRepository.findByEmail("vet@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$tempPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        // Act
        UserResponse response = userService.createStaffUser(
                "Dr. Veterinario",
                "vet@example.com",
                Role.ROLE_VETERINARIO
        );

        // Assert
        assertNotNull(response);
        assertEquals("Dr. Veterinario", response.getName());
        assertEquals("vet@example.com", response.getEmail());
        verify(emailService, times(1)).sendTemporaryPassword(eq("vet@example.com"), anyString());
    }

    @Test
    @DisplayName("Cargar usuario por email debe retornar UserDetails")
    void testLoadUserByUsername() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
    }

    @Test
    @DisplayName("Cargar usuario inexistente debe lanzar UsernameNotFoundException")
    void testLoadUserByUsernameNoExiste() {
        // Arrange
        when(userRepository.findByEmail("noexiste@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("noexiste@example.com")
        );
    }

    @Test
    @DisplayName("Validar contraseña fuerte debe retornar true para contraseña válida")
    void testIsPasswordStrong() {
        // Act & Assert
        assertTrue(userService.isPasswordStrong("Password123!"));
        assertTrue(userService.isPasswordStrong("MyP@ssw0rd"));
        assertFalse(userService.isPasswordStrong("weak"));
        assertFalse(userService.isPasswordStrong("nouppercas3!"));
        assertFalse(userService.isPasswordStrong("NOLOWERCASE1!"));
        assertFalse(userService.isPasswordStrong("NoNumbers!"));
        assertFalse(userService.isPasswordStrong("NoSpecial123"));
    }
}
