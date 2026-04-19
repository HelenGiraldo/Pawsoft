package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.UpdateProfileRequest;
import co.edu.uniquindio.backendpawsoft.dto.UserResponse;
import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.exception.UserNotFoundException;
import co.edu.uniquindio.backendpawsoft.exception.EmailAlreadyExistsException;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import co.edu.uniquindio.backendpawsoft.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User testVet;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildTestUser();
        testVet = TestDataBuilder.buildTestVet();
        testAdmin = TestDataBuilder.buildTestAdmin();
    }

    @Test
    @DisplayName("Should successfully get user by ID")
    void shouldGetUserById() {
        // Given
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserById(testUser.getId());

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getFirstName(), result.getFirstName());

        verify(userRepository).findById(testUser.getId());
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should successfully get user by email")
    void shouldGetUserByEmail() {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserByEmail(testUser.getEmail());

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());

        verify(userRepository).findByEmail(testUser.getEmail());
    }

    @Test
    @DisplayName("Should throw exception when user not found by email")
    void shouldThrowExceptionWhenUserNotFoundByEmail() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(email));

        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should successfully update user profile")
    void shouldUpdateUserProfile() {
        // Given
        UpdateProfileRequest updateRequest = TestDataBuilder.buildUpdateProfileRequest();
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot(updateRequest.getEmail(), testUser.getId())).thenReturn(false);
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        User result = userService.updateProfile(testUser.getId(), updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(updateRequest.getFirstName(), result.getFirstName());
        assertEquals(updateRequest.getLastName(), result.getLastName());
        assertEquals(updateRequest.getPhone(), result.getPhone());

        verify(userRepository).findById(testUser.getId());
        verify(userRepository).save(testUser);
        verify(auditLogService).logProfileUpdated(testUser.getId());
    }

    @Test
    @DisplayName("Should throw exception when updating profile with existing email")
    void shouldThrowExceptionWhenUpdatingProfileWithExistingEmail() {
        // Given
        UpdateProfileRequest updateRequest = TestDataBuilder.buildUpdateProfileRequest();
        updateRequest.setEmail("existing@example.com");
        
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot(updateRequest.getEmail(), testUser.getId())).thenReturn(true);

        // When & Then
        assertThrows(EmailAlreadyExistsException.class, 
                () -> userService.updateProfile(testUser.getId(), updateRequest));

        verify(userRepository).findById(testUser.getId());
        verify(userRepository).existsByEmailAndIdNot(updateRequest.getEmail(), testUser.getId());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully get all users with pagination")
    void shouldGetAllUsersWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = Arrays.asList(testUser, testVet, testAdmin);
        Page<User> userPage = new PageImpl<>(users, pageable, 3);
        
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // When
        Page<UserResponse> result = userService.getAllUsers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(testUser.getEmail(), result.getContent().get(0).getEmail());

        verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should successfully get users by role")
    void shouldGetUsersByRole() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<User> vets = Arrays.asList(testVet);
        Page<User> vetPage = new PageImpl<>(vets, pageable, 1);
        
        when(userRepository.findByRole(Role.VETERINARIAN, pageable)).thenReturn(vetPage);

        // When
        Page<UserResponse> result = userService.getUsersByRole(Role.VETERINARIAN, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testVet.getEmail(), result.getContent().get(0).getEmail());
        assertEquals(Role.VETERINARIAN.name(), result.getContent().get(0).getRole());

        verify(userRepository).findByRole(Role.VETERINARIAN, pageable);
    }

    @Test
    @DisplayName("Should successfully activate user")
    void shouldActivateUser() {
        // Given
        testUser.setActive(false);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        userService.activateUser(testUser.getId());

        // Then
        assertTrue(testUser.isActive());

        verify(userRepository).findById(testUser.getId());
        verify(userRepository).save(testUser);
        verify(auditLogService).logUserActivated(testUser.getId());
    }

    @Test
    @DisplayName("Should successfully deactivate user")
    void shouldDeactivateUser() {
        // Given
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        userService.deactivateUser(testUser.getId());

        // Then
        assertFalse(testUser.isActive());

        verify(userRepository).findById(testUser.getId());
        verify(userRepository).save(testUser);
        verify(auditLogService).logUserDeactivated(testUser.getId());
    }

    @Test
    @DisplayName("Should successfully enable two factor authentication")
    void shouldEnableTwoFactorAuthentication() {
        // Given
        testUser.setTwoFactorEnabled(false);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        userService.enableTwoFactorAuth(testUser.getId());

        // Then
        assertTrue(testUser.isTwoFactorEnabled());

        verify(userRepository).findById(testUser.getId());
        verify(userRepository).save(testUser);
        verify(auditLogService).logTwoFactorEnabled(testUser.getId());
    }

    @Test
    @DisplayName("Should successfully disable two factor authentication")
    void shouldDisableTwoFactorAuthentication() {
        // Given
        testUser.setTwoFactorEnabled(true);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        userService.disableTwoFactorAuth(testUser.getId());

        // Then
        assertFalse(testUser.isTwoFactorEnabled());

        verify(userRepository).findById(testUser.getId());
        verify(userRepository).save(testUser);
        verify(auditLogService).logTwoFactorDisabled(testUser.getId());
    }

    @Test
    @DisplayName("Should successfully change user role")
    void shouldChangeUserRole() {
        // Given
        Role newRole = Role.VETERINARIAN;
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        userService.changeUserRole(testUser.getId(), newRole);

        // Then
        assertEquals(newRole, testUser.getRole());

        verify(userRepository).findById(testUser.getId());
        verify(userRepository).save(testUser);
        verify(auditLogService).logRoleChanged(testUser.getId(), newRole);
    }

    @Test
    @DisplayName("Should successfully search users by name or email")
    void shouldSearchUsersByNameOrEmail() {
        // Given
        String searchTerm = "test";
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);
        
        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                searchTerm, searchTerm, searchTerm, pageable)).thenReturn(userPage);

        // When
        Page<UserResponse> result = userService.searchUsers(searchTerm, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser.getEmail(), result.getContent().get(0).getEmail());

        verify(userRepository).findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                searchTerm, searchTerm, searchTerm, pageable);
    }

    @Test
    @DisplayName("Should get user statistics")
    void shouldGetUserStatistics() {
        // Given
        when(userRepository.countByRole(Role.CLIENT)).thenReturn(100L);
        when(userRepository.countByRole(Role.VETERINARIAN)).thenReturn(10L);
        when(userRepository.countByRole(Role.RECEPTIONIST)).thenReturn(5L);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(2L);
        when(userRepository.countByIsActive(true)).thenReturn(115L);
        when(userRepository.countByIsActive(false)).thenReturn(2L);
        when(userRepository.countByIsTwoFactorEnabled(true)).thenReturn(50L);

        // When
        var stats = userService.getUserStatistics();

        // Then
        assertNotNull(stats);
        assertEquals(100L, stats.get("clients"));
        assertEquals(10L, stats.get("veterinarians"));
        assertEquals(5L, stats.get("receptionists"));
        assertEquals(2L, stats.get("admins"));
        assertEquals(115L, stats.get("active"));
        assertEquals(2L, stats.get("inactive"));
        assertEquals(50L, stats.get("twoFactorEnabled"));

        verify(userRepository).countByRole(Role.CLIENT);
        verify(userRepository).countByRole(Role.VETERINARIAN);
        verify(userRepository).countByRole(Role.RECEPTIONIST);
        verify(userRepository).countByRole(Role.ADMIN);
        verify(userRepository).countByIsActive(true);
        verify(userRepository).countByIsActive(false);
        verify(userRepository).countByIsTwoFactorEnabled(true);
    }

    @Test
    @DisplayName("Should successfully delete user")
    void shouldDeleteUser() {
        // Given
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // When
        userService.deleteUser(testUser.getId());

        // Then
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).delete(testUser);
        verify(auditLogService).logUserDeleted(testUser.getId());
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent user")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("Should check if email exists")
    void shouldCheckIfEmailExists() {
        // Given
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When
        boolean exists = userService.emailExists(email);

        // Then
        assertTrue(exists);
        verify(userRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("Should get available veterinarians")
    void shouldGetAvailableVeterinarians() {
        // Given
        List<User> vets = Arrays.asList(testVet);
        when(userRepository.findByRoleAndIsActiveTrue(Role.VETERINARIAN)).thenReturn(vets);

        // When
        List<UserResponse> result = userService.getAvailableVeterinarians();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testVet.getEmail(), result.get(0).getEmail());
        assertEquals(Role.VETERINARIAN.name(), result.get(0).getRole());

        verify(userRepository).findByRoleAndIsActiveTrue(Role.VETERINARIAN);
    }
}