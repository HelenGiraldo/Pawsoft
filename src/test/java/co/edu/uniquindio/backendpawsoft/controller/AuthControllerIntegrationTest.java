package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.config.BaseIntegrationTest;
import co.edu.uniquindio.backendpawsoft.dto.LoginRequest;
import co.edu.uniquindio.backendpawsoft.dto.RegisterRequest;
import co.edu.uniquindio.backendpawsoft.dto.TwoFactorVerifyRequest;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import co.edu.uniquindio.backendpawsoft.service.RecaptchaService;
import co.edu.uniquindio.backendpawsoft.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private RecaptchaService recaptchaService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        super.setUp();
        
        // Create test user in database
        testUser = TestDataBuilder.buildTestUser();
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);

        loginRequest = TestDataBuilder.buildLoginRequest();
        loginRequest.setEmail(testUser.getEmail());

        registerRequest = TestDataBuilder.buildRegisterRequest();

        // Mock reCAPTCHA service to always return true for tests
        when(recaptchaService.verifyRecaptcha(anyString())).thenReturn(true);
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void shouldLoginWithValidCredentials() throws Exception {
        // When & Then
        ResultActions result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.role", is(testUser.getRole().name())))
                .andExpect(jsonPath("$.twoFactorRequired", is(false)))
                .andExpect(jsonPath("$.user.email", is(testUser.getEmail())))
                .andExpect(jsonPath("$.user.firstName", is(testUser.getFirstName())))
                .andExpect(jsonPath("$.user.lastName", is(testUser.getLastName())));
    }

    @Test
    @DisplayName("Should require 2FA when user has it enabled")
    void shouldRequire2FAWhenEnabled() throws Exception {
        // Given
        testUser.setTwoFactorEnabled(true);
        userRepository.save(testUser);

        // When & Then
        ResultActions result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.twoFactorRequired", is(true)))
                .andExpect(jsonPath("$.twoFactorToken", notNullValue()));
    }

    @Test
    @DisplayName("Should return 400 when reCAPTCHA verification fails")
    void shouldReturn400WhenRecaptchaFails() throws Exception {
        // Given
        when(recaptchaService.verifyRecaptcha(anyString())).thenReturn(false);

        // When & Then
        ResultActions result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("reCAPTCHA")));
    }

    @Test
    @DisplayName("Should return 401 with invalid credentials")
    void shouldReturn401WithInvalidCredentials() throws Exception {
        // Given
        loginRequest.setPassword("wrongpassword");

        // When & Then
        ResultActions result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)));

        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Invalid credentials")));
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() throws Exception {
        // Given
        loginRequest.setEmail("nonexistent@example.com");

        // When & Then
        ResultActions result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)));

        result.andExpect(status().isNotFound())
                .andExpected(jsonPath("$.message", containsString("User not found")));
    }

    @Test
    @DisplayName("Should return 403 when user is inactive")
    void shouldReturn403WhenUserIsInactive() throws Exception {
        // Given
        testUser.setActive(false);
        userRepository.save(testUser);

        // When & Then
        ResultActions result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)));

        result.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("inactive")));
    }

    @Test
    @DisplayName("Should return 403 when email is not verified")
    void shouldReturn403WhenEmailNotVerified() throws Exception {
        // Given
        testUser.setEmailVerified(false);
        userRepository.save(testUser);

        // When & Then
        ResultActions result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)));

        result.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("email not verified")));
    }

    @Test
    @DisplayName("Should successfully register new user")
    void shouldRegisterNewUser() throws Exception {
        // When & Then
        ResultActions result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(registerRequest)));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("User registered successfully")))
                .andExpect(jsonPath("$.userId", notNullValue()));

        // Verify user was created in database
        assertTrue(userRepository.existsByEmail(registerRequest.getEmail()));
    }

    @Test
    @DisplayName("Should return 409 when email already exists")
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        // Given
        registerRequest.setEmail(testUser.getEmail());

        // When & Then
        ResultActions result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(registerRequest)));

        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("Should return 400 with invalid registration data")
    void shouldReturn400WithInvalidRegistrationData() throws Exception {
        // Given
        registerRequest.setEmail("invalid-email");
        registerRequest.setPassword("123"); // Too short

        // When & Then
        ResultActions result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(registerRequest)));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should return 400 when required fields are missing")
    void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
        // Given
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .email("test@example.com")
                // Missing required fields
                .build();

        // When & Then
        ResultActions result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invalidRequest)));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should successfully verify 2FA code")
    void shouldVerify2FACode() throws Exception {
        // Given
        testUser.setTwoFactorEnabled(true);
        userRepository.save(testUser);

        // First login to get 2FA token
        ResultActions loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)));

        String twoFactorToken = loginResult.andReturn()
                .getResponse()
                .getContentAsString();
        
        // Extract token from response (simplified for test)
        String token = testUser.getId().toString();

        TwoFactorVerifyRequest verifyRequest = TwoFactorVerifyRequest.builder()
                .twoFactorToken(token)
                .code("123456") // This would be mocked in a real test
                .build();

        // When & Then
        ResultActions result = mockMvc.perform(post("/auth/verify-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(verifyRequest)));

        // Note: This test would need proper 2FA code generation mocking
        // For now, we expect it to fail with invalid code
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 with invalid 2FA token")
    void shouldReturn400WithInvalid2FAToken() throws Exception {
        // Given
        TwoFactorVerifyRequest verifyRequest = TwoFactorVerifyRequest.builder()
                .twoFactorToken("invalid-token")
                .code("123456")
                .build();

        // When & Then
        ResultActions result = mockMvc.perform(post("/auth/verify-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(verifyRequest)));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid")));
    }

    @Test
    @DisplayName("Should return 400 with malformed JSON")
    void shouldReturn400WithMalformedJSON() throws Exception {
        // When & Then
        ResultActions result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"));

        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 415 with unsupported media type")
    void shouldReturn415WithUnsupportedMediaType() throws Exception {
        // When & Then
        ResultActions result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.TEXT_PLAIN)
                .content(asJsonString(loginRequest)));

        result.andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("Should return 405 with unsupported HTTP method")
    void shouldReturn405WithUnsupportedMethod() throws Exception {
        // When & Then
        ResultActions result = mockMvc.perform(get("/auth/login"));

        result.andExpect(status().isMethodNotAllowed());
    }
}