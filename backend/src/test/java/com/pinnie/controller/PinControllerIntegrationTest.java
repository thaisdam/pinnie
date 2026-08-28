package com.pinnie.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinnie.dto.PinCreateRequestDTO;
import com.pinnie.model.PendingImageUpload;
import com.pinnie.model.User;
import com.pinnie.repository.PendingImageUploadRepository;
import com.pinnie.repository.PinRepository;
import com.pinnie.repository.UserRepository;
import com.pinnie.security.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class PinControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private PendingImageUploadRepository pendingImageUploadRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private User owner;
    private String ownerJwt;
    private String xsrfToken;
    private Cookie csrfCookie;

    @BeforeEach
    void setUp() throws Exception {
        pinRepository.deleteAll();
        pendingImageUploadRepository.deleteAll();
        userRepository.deleteAll();

        owner = new User("owner", "owner@test.com", "hash", "Owner");
        owner = userRepository.save(owner);
        ownerJwt = jwtService.generateToken(owner.getId().toString());

        MvcResult csrfResult = mockMvc.perform(get("/api/csrf")).andReturn();
        csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
        xsrfToken = csrfCookie.getValue();
    }

    @Test
    void createPin_ConsumesUploadIdAndCreatesPin() throws Exception {
        // Arrange
        PendingImageUpload pending = new PendingImageUpload(
                UUID.randomUUID(), owner, "test.jpg", "image/jpeg", 1000, 800, 600, Instant.now().plusSeconds(3600)
        );
        pendingImageUploadRepository.save(pending);

        PinCreateRequestDTO request = new PinCreateRequestDTO("My Pin", "Desc", "http://test.com", "alt", pending.getId());

        // Act & Assert
        mockMvc.perform(post("/api/pins")
                .header("X-XSRF-TOKEN", xsrfToken)
                .cookie(new Cookie("pinnie_jwt", ownerJwt), csrfCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("My Pin"))
                .andExpect(jsonPath("$.imageUrl").value("/uploads/test.jpg"));

        assertFalse(pendingImageUploadRepository.existsById(pending.getId()));
    }
}
