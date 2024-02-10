package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

    @MockBean
    private ArticlesRepository articlesRepository;

    @MockBean
    private UserRepository userRepository;

    // Tests for GET /api/articles/all
    @Test
    public void loggedOutUsersCannotGetAll() throws Exception {
        mockMvc.perform(get("/api/articles/all"))
                .andExpect(status().isForbidden()); // Logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void loggedInUsersCanGetAll() throws Exception {
        mockMvc.perform(get("/api/articles/all"))
                .andExpect(status().isOk()); // Logged in users can get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void loggedInUserCanGetAllArticles() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Articles article1 = Articles.builder()
                .title("Article 1")
                .url("http://example.com/1")
                .explanation("Explanation 1")
                .email("user1@example.com")
                .dateAdded(now)
                .build();

        Articles article2 = Articles.builder()
                .title("Article 2")
                .url("http://example.com/2")
                .explanation("Explanation 2")
                .email("user2@example.com")
                .dateAdded(now)
                .build();

        ArrayList<Articles> expectedArticles = new ArrayList<>();
        expectedArticles.addAll(Arrays.asList(article1, article2));

        when(articlesRepository.findAll()).thenReturn(expectedArticles);

        MvcResult response = mockMvc.perform(get("/api/articles/all"))
                .andExpect(status().isOk()).andReturn();

        verify(articlesRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedArticles);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // Tests for POST /api/articles/post
    @Test
    public void loggedOutUsersCannotPost() throws Exception {
        mockMvc.perform(post("/api/articles/post"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = { "ADMIN" })
    @Test
    public void adminUserCanPostANewArticle() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Articles newArticle = Articles.builder()
                .title("New Article")
                .url("http://newexample.com")
                .explanation("New Explanation")
                .email("admin@example.com")
                .dateAdded(now)
                .build();

        when(articlesRepository.save(any(Articles.class))).thenReturn(newArticle);

        MvcResult response = mockMvc.perform(post("/api/articles/post")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(newArticle))
                .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        verify(articlesRepository, times(1)).save(any(Articles.class));
        String expectedJson = mapper.writeValueAsString(newArticle);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }
}
