package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.config.BaseIntegrationTest;
import co.edu.uniquindio.backendpawsoft.dto.LoginRequest;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import co.edu.uniquindio.backendpawsoft.service.RecaptchaService;
import co.edu.uniquindio.backendpawsoft.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @MockitoBean
    private RecaptchaService recaptchaService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUpData() {
        testUser = TestDataBuilder.buildTestUser();
        testUser.setId(null); // let DB assign ID
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);

        loginRequest = TestDataBuilder.buildLoginRequest();
        loginRequest.setEmail(testUser.getEmail());

        when(recaptchaService.isValid(anyString())).thenReturn(true);
    }

    @Test
    @DisplayName("Should return 200 and send 2FA code on valid login")
    void shouldReturn200OnValidLogin() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.email", is(testUser.getEmail())))
                .andExpect(jsonPath("$.role", is(testUser.getRole().name())));
    }

    @Test
    @DisplayName("Should return 401 when reCAPTCHA fails")
    void shouldReturn401WhenRecaptchaFails() throws Exception {
        when(recaptchaService.isValid(anyString())).thenReturn(false);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() throws Exception {
        loginRequest.setEmail("nonexistent@example.com");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 when password is wrong")
    void shouldReturn401WhenPasswordIsWrong() throws Exception {
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 when required fields are missing")
    void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
        LoginRequest invalid = new LoginRequest();
        // missing email, password, recaptchaToken

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 with malformed JSON")
    void shouldReturn400WithMalformedJson() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 415 with unsupported media type")
    void shouldReturn415WithUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.TEXT_PLAIN)
                .content(asJsonString(loginRequest)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("Should return 500 when email is not verified")
    void shouldReturnErrorWhenEmailNotVerified() throws Exception {
        testUser.setEnabled(false);
        userRepository.save(testUser);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)))
                .andExpect(status().is5xxServerError());
    }
}
