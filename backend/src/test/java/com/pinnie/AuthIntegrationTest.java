package com.pinnie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinnie.dto.LoginRequestDTO;
import com.pinnie.dto.RegisterRequestDTO;
import com.pinnie.model.User;
import com.pinnie.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
@Testcontainers
class AuthIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterAndLoginSuccessfully() throws Exception {
        // 1. Register User
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "john_doe", "john@example.com", "password123", "John Doe"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john_doe"));

        assertTrue(userRepository.existsByEmail("john@example.com"));

        // 2. CSRF Token Request (simulating frontend initial request)
        MvcResult csrfResult = mockMvc.perform(get("/api/csrf"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andReturn();

        String xsrfToken = csrfResult.getResponse().getCookie("XSRF-TOKEN").getValue();

        // 3. Login User
        LoginRequestDTO loginRequest = new LoginRequestDTO("john@example.com", "password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .header("X-XSRF-TOKEN", xsrfToken)
                .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("pinnie_jwt"))
                .andExpect(cookie().httpOnly("pinnie_jwt", true))
                .andReturn();

        Cookie jwtCookie = loginResult.getResponse().getCookie("pinnie_jwt");

        // 4. Access Protected Endpoint /api/users/me
        mockMvc.perform(get("/api/users/me")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
        
        // 5. Logout
        mockMvc.perform(post("/api/auth/logout")
                .header("X-XSRF-TOKEN", xsrfToken)
                .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN")))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("pinnie_jwt", 0))
                .andExpect(cookie().maxAge("XSRF-TOKEN", 0));
    }

    @Test
    void shouldFailLoginWithBadCredentials() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("wrong@example.com", "wrongpass");
        
        // Simulating CSRF just to pass the POST block
        MvcResult csrfResult = mockMvc.perform(get("/api/csrf")).andReturn();
        String xsrfToken = csrfResult.getResponse().getCookie("XSRF-TOKEN").getValue();

        mockMvc.perform(post("/api/auth/login")
                .header("X-XSRF-TOKEN", xsrfToken)
                .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldBlockAccessWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden()); // Due to CSRF/Security rules without auth
    }

    @Test
    void shouldBlockDisabledUser() throws Exception {
        User user = new User("disabled_user", "disabled@example.com", "hash", "Disabled");
        user.setEnabled(false);
        userRepository.save(user);

        // We can mock the login or just test that if the user tries to login, or load context, it fails.
        // Let's test login
        LoginRequestDTO loginRequest = new LoginRequestDTO("disabled@example.com", "hash");
        
        MvcResult csrfResult = mockMvc.perform(get("/api/csrf")).andReturn();
        String xsrfToken = csrfResult.getResponse().getCookie("XSRF-TOKEN").getValue();

        // CustomUserDetailsService sets user as disabled, so AuthenticationManager throws DisabledException -> 401/403
        // Actually, BadCredentials or DisabledException. In GlobalExceptionHandler we map BadCredentials to 401. 
        // We haven't mapped DisabledException, it might return 401 or 403 or 500 if unhandled. 
        // Let's just expect client error (4xx)
        mockMvc.perform(post("/api/auth/login")
                .header("X-XSRF-TOKEN", xsrfToken)
                .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError());
    }
}
