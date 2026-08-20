package com.pinnie.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinnie.dto.BoardCreateRequestDTO;
import com.pinnie.dto.BoardUpdateRequestDTO;
import com.pinnie.model.Board;
import com.pinnie.model.User;
import com.pinnie.repository.BoardRepository;
import com.pinnie.repository.UserRepository;
import com.pinnie.security.JwtService;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class BoardControllerIntegrationTest {

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

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private JwtService jwtService;

    private User owner;
    private User otherUser;
    private String ownerJwt;
    private String otherUserJwt;
    private String xsrfToken;
    private Cookie csrfCookie;

    @BeforeEach
    void setUp() throws Exception {
        boardRepository.deleteAll();
        userRepository.deleteAll();

        owner = new User("owner", "owner@test.com", "hash", "Owner");
        owner = userRepository.save(owner);
        ownerJwt = jwtService.generateToken(owner.getId().toString());

        otherUser = new User("other", "other@test.com", "hash", "Other");
        otherUser = userRepository.save(otherUser);
        otherUserJwt = jwtService.generateToken(otherUser.getId().toString());

        MvcResult csrfResult = mockMvc.perform(get("/api/csrf")).andReturn();
        csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
        xsrfToken = csrfCookie.getValue();
    }

    @Test
    void shouldCreateBoardSuccessfully() throws Exception {
        BoardCreateRequestDTO request = new BoardCreateRequestDTO("My Board", "Desc", false);

        mockMvc.perform(post("/api/boards")
                .header("X-XSRF-TOKEN", xsrfToken)
                .cookie(new Cookie("pinnie_jwt", ownerJwt), csrfCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("My Board"))
                .andExpect(jsonPath("$.userId").value(owner.getId().toString()));
    }

    @Test
    void shouldReturn404WhenOtherUserAccessesPrivateBoard() throws Exception {
        Board privateBoard = new Board("Private", "Secret", true, owner);
        privateBoard = boardRepository.save(privateBoard);

        mockMvc.perform(get("/api/boards/" + privateBoard.getId())
                .cookie(new Cookie("pinnie_jwt", otherUserJwt)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200WhenOwnerAccessesPrivateBoard() throws Exception {
        Board privateBoard = new Board("Private", "Secret", true, owner);
        privateBoard = boardRepository.save(privateBoard);

        mockMvc.perform(get("/api/boards/" + privateBoard.getId())
                .cookie(new Cookie("pinnie_jwt", ownerJwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Private"));
    }

    @Test
    void shouldReturn200WhenOtherUserAccessesPublicBoard() throws Exception {
        Board publicBoard = new Board("Public", "Public", false, owner);
        publicBoard = boardRepository.save(publicBoard);

        mockMvc.perform(get("/api/boards/" + publicBoard.getId())
                .cookie(new Cookie("pinnie_jwt", otherUserJwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Public"));
    }

    @Test
    void shouldUpdateBoardSuccessfullyAsOwner() throws Exception {
        Board board = new Board("Public", "Public", false, owner);
        board = boardRepository.save(board);

        BoardUpdateRequestDTO request = new BoardUpdateRequestDTO("Updated", "Updated desc", true);

        mockMvc.perform(put("/api/boards/" + board.getId())
                .header("X-XSRF-TOKEN", xsrfToken)
                .cookie(new Cookie("pinnie_jwt", ownerJwt), csrfCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.private").value(true));
    }

    @Test
    void shouldReturn404WhenUpdatingOtherUsersBoard() throws Exception {
        Board board = new Board("Public", "Public", false, owner);
        board = boardRepository.save(board);

        BoardUpdateRequestDTO request = new BoardUpdateRequestDTO("Updated", "Updated desc", true);

        mockMvc.perform(put("/api/boards/" + board.getId())
                .header("X-XSRF-TOKEN", xsrfToken)
                .cookie(new Cookie("pinnie_jwt", otherUserJwt), csrfCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteBoardSuccessfullyAsOwner() throws Exception {
        Board board = new Board("Public", "Public", false, owner);
        board = boardRepository.save(board);

        mockMvc.perform(delete("/api/boards/" + board.getId())
                .header("X-XSRF-TOKEN", xsrfToken)
                .cookie(new Cookie("pinnie_jwt", ownerJwt), csrfCookie))
                .andExpect(status().isNoContent());

        assertFalse(boardRepository.existsById(board.getId()));
    }
}
