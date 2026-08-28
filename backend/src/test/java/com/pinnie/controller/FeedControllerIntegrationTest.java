package com.pinnie.controller;

import com.pinnie.model.Pin;
import com.pinnie.model.User;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FeedControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private User owner;
    private Pin pin1;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        pinRepository.deleteAll();
        userRepository.deleteAll();

        owner = new User("owner", "owner@test.com", "hash", "Owner");
        owner = userRepository.save(owner);
        jwtToken = jwtService.generateToken(owner.getId().toString());

        pin1 = new Pin(owner, "Pin 1", "Desc", "url", "alt", "file1.jpg", "image/jpeg", 1L, 1, 1);
        pinRepository.save(pin1);
    }

    @Test
    void getFeed_PublicAccess_Success() throws Exception {
        mockMvc.perform(get("/api/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(1)))
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void getFeed_AuthenticatedAccess_Success() throws Exception {
        mockMvc.perform(get("/api/feed")
                .cookie(new Cookie("pinnie_jwt", jwtToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(1)));
    }

    @Test
    void getFeed_PaginationAndSortIgnore_Success() throws Exception {
        mockMvc.perform(get("/api/feed?page=0&size=10&sort=title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageable.pageSize", is(10)))
                .andExpect(jsonPath("$.pageable.sort.sorted", is(true)));
    }
}
