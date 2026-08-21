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
class SearchControllerIntegrationTest {

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

        pin1 = new Pin(owner, "Bolo de Cenoura", "Receita especial", "url", "alt", "file1.jpg", "image/jpeg", 1L, 1, 1);
        pinRepository.save(pin1);
    }

    @Test
    void search_PublicAccess_Success() throws Exception {
        mockMvc.perform(get("/api/search?q=bolo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(1)))
                .andExpect(jsonPath("$.content[0].title", is("Bolo de Cenoura")))
                .andExpect(jsonPath("$.hasNext", is(false)));
    }

    @Test
    void search_AuthenticatedAccess_Success() throws Exception {
        mockMvc.perform(get("/api/search?q=cenoura")
                .cookie(new Cookie("pinnie_jwt", jwtToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(1)));
    }

    @Test
    void search_ByDescription_Success() throws Exception {
        mockMvc.perform(get("/api/search?q=receita"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(1)));
    }

    @Test
    void search_CaseInsensitive_Success() throws Exception {
        mockMvc.perform(get("/api/search?q=BOLO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(1)));
    }

    @Test
    void search_NoResults_Success() throws Exception {
        mockMvc.perform(get("/api/search?q=viagem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(0)));
    }

    @Test
    void search_PaginationAndSortIgnore_Success() throws Exception {
        mockMvc.perform(get("/api/search?q=bolo&page=0&size=50&sort=title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageable.pageSize", is(50)))
                .andExpect(jsonPath("$.pageable.sort.sorted", is(true)));
    }

    @Test
    void search_QueryTooShort_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/search?q=a"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_QueryEmpty_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/search?q=   "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_QueryTooLong_ReturnsBadRequest() throws Exception {
        String longQuery = "a".repeat(101);
        mockMvc.perform(get("/api/search?q=" + longQuery))
                .andExpect(status().isBadRequest());
    }
}
