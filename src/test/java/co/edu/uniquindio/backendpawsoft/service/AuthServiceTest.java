package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.*;
import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.exception.*;
import co.edu.uniquindio.backendpawsoft.model.RefreshToken;
import co.edu.uniquindio.backendpawsoft.model.TwoFactorAuth;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import co.edu.uniquindio.backendpawsoft.security.JwtService;
import co.edu.uniquindio.backendpawsoft.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @Mock
    private RecaptchaService recaptchaService;

    @Mock
    private TwoFactorService twoFactorService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildTestUser();
        loginRequest = TestDataBuilder.buildLoginRequest();
        registerRequest = TestDataBuilder.buildRegisterRequest();
    }

    @Test
    @DisplayName("Should successfully login user with valid credentials")
    void shouldLoginUserWithValidCredentials() {
        // Given
        when(recaptchaService.verifyRecaptcha(anyString())).thenReturn(true);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");
        
        RefreshToken refreshToken = TestDataBuilder.buildTestRefreshToken(testUser);
        when(refreshTokenService.createRefreshToken(testUser.getId())).thenReturn(refreshToken);

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());
        assertEquals(refreshToken.getToken(), response.getRefreshToken());
        assertEquals(testUser.getRole().name(), response.getRole());
        assertFalse(response.isTwoFactorRequired());
        
        verify(recaptchaService).verifyRecaptcha(loginRequest.getRecaptchaToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(testUser);
        verify(refreshTokenService).createRefreshToken(testUser.getId());
    }

    @Test
    @DisplayName("Should require 2FA when user has it enabled")
    void shouldRequire2FAWhenUserHasItEnabled() {
        // Given
        testUser.setTwoFactorEnabled(true);
        when(recaptchaService.verifyRecaptcha(anyString())).thenReturn(true);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        
        TwoFactorAuth twoFactorAuth = TestDataBuilder.buildTestTwoFactorAuth(testUser);
        when(twoFactorService.generateTwoFactorCode(testUser)).thenReturn(twoFactorAuth);

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertNull(response.getAccessToken());
        assertNull(response.getRefreshToken());
        assertTrue(response.isTwoFactorRequired());
        assertEquals(testUser.getId().toString(), response.getTwoFactorToken());
        
        verify(twoFactorService).generateTwoFactorCode(testUser);
        verify(emailService).sendTwoFactorCode(testUser.getEmail(), twoFactorAuth.getCode());
    }

    @Test
    @DisplayName("Should throw exception when reCAPTCHA verification fails")
    void shouldThrowExceptionWhenRecaptchaFails() {
        // Given
        when(recaptchaService.verifyRecaptcha(anyString())).thenReturn(false);

        // When & Then
        assertThrows(InvalidRecaptchaException.class, () -> authService.login(loginRequest));
        
        verify(recaptchaService).verifyRecaptcha(loginRequest.getRecaptchaToken());
        verifyNoInteractions(authenticationManager);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(recaptchaService.verifyRecaptcha(anyString())).thenReturn(true);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> authService.login(loginRequest));
        
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verifyNoInteractions(authenticationManager);
    }

    @Test
    @DisplayName("Should throw exception when user is inactive")
    void shouldThrowExceptionWhenUserIsInactive() {
        // Given
        testUser.setActive(false);
        when(recaptchaService.verifyRecaptcha(anyString())).thenReturn(true);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(UserInactiveException.class, () -> authService.login(loginRequest));
        
        verifyNoInteractions(authenticationManager);
    }

    @Test
    @DisplayName("Should throw exception when email is not verified")
    void shouldThrowExceptionWhenEmailNotVerified() {
        // Given
        testUser.setEmailVerified(false);
        when(recaptchaService.verifyRecaptcha(anyString())).thenReturn(true);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(EmailNotVerifiedException.class, () -> authService.login(loginRequest));
        
        verifyNoInteractions(authenticationManager);
    }

    @Test
    @DisplayName("Should throw exception when authentication fails")
    void shouldThrowExceptionWhenAuthenticationFails() {
        // Given
        when(recaptchaService.verifyRecaptcha(anyString())).thenReturn(true);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should successfully register new user")
    void shouldRegisterNewUser() {
        // Given
        when(recaptchaService.verifyRecaptcha(anyString())).thenReturn(true);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded-password");
        
        User savedUser = TestDataBuilder.buildTestUser();
        savedUser.setEmail(registerRequest.getEmail());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        RegisterResponse response = authService.register(registerRequest);

        // Then
        assertNotNull(response);
        assertEquals("User registered successfully", response.getMessage());
        assertEquals(savedUser.getId(), response.getUserId());
        
        verify(recaptchaService).verifyRecaptcha(registerRequest.getRecaptchaToken());
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(eq(savedUser.getEmail()), anyString());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        when(recaptchaService.verifyRecaptcha(anyString())).thenReturn(true);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(registerRequest));
        
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully verify 2FA code")
    void shouldVerify2FACode() {
        // Given
        String twoFactorToken = testUser.getId().toString();
        String code = "123456";
        
        TwoFactorVerifyRequest request = TwoFactorVerifyRequest.builder()
                .twoFactorToken(twoFactorToken)
                .code(code)
                .build();

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(twoFactorService.verifyTwoFactorCode(testUser, code)).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");
        
        RefreshToken refreshToken = TestDataBuilder.buildTestRefreshToken(testUser);
        when(refreshTokenService.createRefreshToken(testUser.getId())).thenReturn(refreshToken);

        // When
        LoginResponse response = authService.verifyTwoFactor(request);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());
        assertEquals(refreshToken.getToken(), response.getRefreshToken());
        assertEquals(testUser.getRole().name(), response.getRole());
        assertFalse(response.isTwoFactorRequired());
        
        verify(twoFactorService).verifyTwoFactorCode(testUser, code);
        verify(jwtService).generateToken(testUser);
        verify(refreshTokenService).createRefreshToken(testUser.getId());
    }

    @Test
    @DisplayName("Should throw exception when 2FA code is invalid")
    void shouldThrowExceptionWhenTwoFactorCodeIsInvalid() {
        // Given
        String twoFactorToken = testUser.getId().toString();
        String code = "invalid";
        
        TwoFactorVerifyRequest request = TwoFactorVerifyRequest.builder()
                .twoFactorToken(twoFactorToken)
                .code(code)
                .build();

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(twoFactorService.verifyTwoFactorCode(testUser, code)).thenReturn(false);

        // When & Then
        assertThrows(InvalidTwoFactorCodeException.class, () -> authService.verifyTwoFactor(request));
        
        verify(twoFactorService).verifyTwoFactorCode(testUser, code);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    @DisplayName("Should successfully refresh token")
    void shouldRefreshToken() {
        // Given
        String refreshTokenString = "refresh-token";
        RefreshToken refreshToken = TestDataBuilder.buildTestRefreshToken(testUser);
        refreshToken.setToken(refreshTokenString);
        
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(refreshTokenString)
                .build();

        when(refreshTokenService.findByToken(refreshTokenString)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(jwtService.generateToken(testUser)).thenReturn("new-jwt-token");

        // When
        RefreshTokenResponse response = authService.refreshToken(request);

        // Then
        assertNotNull(response);
        assertEquals("new-jwt-token", response.getAccessToken());
        assertEquals(refreshTokenString, response.getRefreshToken());
        
        verify(refreshTokenService).findByToken(refreshTokenString);
        verify(refreshTokenService).verifyExpiration(refreshToken);
        verify(jwtService).generateToken(testUser);
    }

    @Test
    @DisplayName("Should throw exception when refresh token is invalid")
    void shouldThrowExceptionWhenRefreshTokenIsInvalid() {
        // Given
        String refreshTokenString = "invalid-token";
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(refreshTokenString)
                .build();

        when(refreshTokenService.findByToken(refreshTokenString)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvalidRefreshTokenException.class, () -> authService.refreshToken(request));
        
        verify(refreshTokenService).findByToken(refreshTokenString);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Should successfully logout user")
    void shouldLogoutUser() {
        // Given
        String refreshTokenString = "refresh-token";
        RefreshToken refreshToken = TestDataBuilder.buildTestRefreshToken(testUser);
        
        when(refreshTokenService.findByToken(refreshTokenString)).thenReturn(Optional.of(refreshToken));

        // When
        authService.logout(refreshTokenString);

        // Then
        verify(refreshTokenService).findByToken(refreshTokenString);
        verify(refreshTokenService).deleteByToken(refreshTokenString);
    }

    @Test
    @DisplayName("Should successfully verify email")
    void shouldVerifyEmail() {
        // Given
        String token = "verification-token";
        testUser.setEmailVerified(false);
        
        when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        authService.verifyEmail(token);

        // Then
        assertTrue(testUser.isEmailVerified());
        assertNull(testUser.getEmailVerificationToken());
        verify(userRepository).findByEmailVerificationToken(token);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when email verification token is invalid")
    void shouldThrowExceptionWhenEmailVerificationTokenIsInvalid() {
        // Given
        String token = "invalid-token";
        when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvalidVerificationTokenException.class, () -> authService.verifyEmail(token));
        
        verify(userRepository).findByEmailVerificationToken(token);
        verify(userRepository, never()).save(any(User.class));
    }
}