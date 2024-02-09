package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

    @MockBean
    private ArticlesRepository articlesRepository;

    @MockBean
    private UserRepository userRepository;

    // Sample article for use in tests
    private final Articles sampleArticle = Articles.builder()
            .id(1L)
            .title("Test Article")
            .url("https://example.com")
            .explanation("This is a test article")
            .email("test@example.com")
            .dateAdded(LocalDateTime.now())
            .build();

    // Tests for GET /api/articles/all
    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/articles/all"))
                .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        when(articlesRepository.findAll()).thenReturn(Arrays.asList(sampleArticle));
        mockMvc.perform(get("/api/articles/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Article"));
    }

    // Tests for POST /api/articles/post
    @WithMockUser(roles = { "ADMIN" })
    @Test
    public void admin_can_post_new_article() throws Exception {
        when(articlesRepository.save(any(Articles.class))).thenReturn(sampleArticle);
        mockMvc.perform(post("/api/articles/post")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(sampleArticle))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Article"));
    }

    // Tests for GET /api/articles?id={id}
    @WithMockUser(roles = { "USER" })
    @Test
    public void user_can_get_article_by_id() throws Exception {
        when(articlesRepository.findById(eq(sampleArticle.getId()))).thenReturn(Optional.of(sampleArticle));
        mockMvc.perform(get("/api/articles")
                .param("id", String.valueOf(sampleArticle.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Article"));
    }

    // Tests for DELETE /api/articles?id={id}
    @WithMockUser(roles = { "ADMIN" })
    @Test
    public void admin_can_delete_article() throws Exception {
        when(articlesRepository.findById(eq(sampleArticle.getId()))).thenReturn(Optional.of(sampleArticle));
        mockMvc.perform(delete("/api/articles")
                .param("id", String.valueOf(sampleArticle.getId()))
                .with(csrf()))
                .andExpect(status().isOk());
        verify(articlesRepository).delete(sampleArticle);
    }

    // Tests for PUT /api/articles?id={id}
    @WithMockUser(roles = { "ADMIN" })
    @Test
    public void admin_can_update_article() throws Exception {
        Articles updatedArticle = Articles.builder()
                .id(sampleArticle.getId())
                .title("Updated Test Article")
                .url("https://example.com/updated")
                .explanation("This is an updated test article")
                .email("updated@example.com")
                .dateAdded(LocalDateTime.now())
                .build();

        when(articlesRepository.findById(eq(sampleArticle.getId()))).thenReturn(Optional.of(sampleArticle));
        when(articlesRepository.save(any(Articles.class))).thenReturn(updatedArticle);

        mockMvc.perform(put("/api/articles")
                .param("id", String.valueOf(sampleArticle.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updatedArticle))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Test Article"));
        verify(articlesRepository).save(any(Articles.class)); // Verify article was saved
    }
}
