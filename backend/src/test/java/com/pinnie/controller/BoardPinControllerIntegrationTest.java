package com.pinnie.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinnie.model.Board;
import com.pinnie.model.BoardPin;
import com.pinnie.model.Pin;
import com.pinnie.model.User;
import com.pinnie.repository.BoardPinRepository;
import com.pinnie.repository.BoardRepository;
import com.pinnie.repository.PinRepository;
import com.pinnie.repository.UserRepository;
import com.pinnie.security.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BoardPinControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BoardPinRepository boardPinRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private User owner;
    private Board board;
    private Pin pin;
    private String ownerJwt;
    private String xsrfToken;
    private Cookie csrfCookie;

    @BeforeEach
    void setUp() throws Exception {
        boardPinRepository.deleteAll();
        boardRepository.deleteAll();
        pinRepository.deleteAll();
        userRepository.deleteAll();

        owner = new User("owner", "owner@test.com", "hash", "Owner");
        owner = userRepository.save(owner);
        ownerJwt = jwtService.generateToken(owner.getId().toString());

        board = new Board("Public Board", "Desc", false, owner);
        board = boardRepository.save(board);

        pin = new Pin(owner, "Pin", "Desc", "url", "alt", "file.jpg", "image/jpeg", 1L, 1, 1);
        pin = pinRepository.save(pin);

        MvcResult csrfResult = mockMvc.perform(get("/api/csrf")).andReturn();
        csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
        xsrfToken = csrfCookie.getValue();
    }

    @Test
    void savePinToBoard_Success() throws Exception {
        mockMvc.perform(post("/api/boards/" + board.getId() + "/pins/" + pin.getId())
                .header("X-XSRF-TOKEN", xsrfToken)
                .cookie(new Cookie("pinnie_jwt", ownerJwt), csrfCookie))
                .andExpect(status().isCreated());

        assertTrue(boardPinRepository.existsByBoardIdAndPinId(board.getId(), pin.getId()));
    }
}
