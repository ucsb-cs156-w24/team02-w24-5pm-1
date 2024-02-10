package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import static org.mockito.AdditionalAnswers.returnsFirstArg;



import static org.hamcrest.Matchers.is;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
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
                                .andExpect(status().isForbidden()); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void loggedInUsersCanGetAll() throws Exception {
                mockMvc.perform(get("/api/articles/all"))
                                .andExpect(status().isOk()); // logged in users can get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void loggedInUserCanGetAllArticles() throws Exception {
                LocalDateTime now = LocalDateTime.now();

                Articles article1 = new Articles(1L, "Article 1 Title", "http://example.com/1", "Article 1 Explanation",
                                "user1@example.com", now);
                Articles article2 = new Articles(2L, "Article 2 Title", "http://example.com/2", "Article 2 Explanation",
                                "user2@example.com", now);

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
                                .title("New Article Title")
                                .url("http://newexample.com")
                                .explanation("New Article Explanation")
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
        @Test
        @WithMockUser(roles = {"ADMIN"})
        public void updateArticle_success() throws Exception {
                LocalDateTime now = LocalDateTime.now();
                Articles existingArticle = new Articles(1L, "Original Title", "http://original.com", "Original Explanation", "admin@ucsb.edu", now);
                Articles updatedArticle = new Articles(1L, "Updated Title", "http://updated.com", "Updated Explanation", "admin@ucsb.edu", now);

                when(articlesRepository.findById(1L)).thenReturn(Optional.of(existingArticle));
                when(articlesRepository.save(any(Articles.class))).thenReturn(updatedArticle);

                mockMvc.perform(put("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatedArticle))
                        .param("id", "1")
                        .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.title", is("Updated Title")));

                verify(articlesRepository).save(any(Articles.class)); // Verifies that save was called, indicating an update occurred.
        }

        @Test
        @WithMockUser(roles = {"ADMIN"})
        public void updateNonExistingArticle_returnsNotFound() throws Exception {
                Long nonExistentId = 999L;
                Articles updatedArticle = new Articles(); // Simplify the object creation for clarity
                updatedArticle.setTitle("Updated Title");

                when(articlesRepository.findById(nonExistentId)).thenReturn(Optional.empty());

                mockMvc.perform(put("/api/articles/{id}", nonExistentId) // Ensure this matches your controller's path variable setup
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatedArticle))
                        .with(csrf()))
                        .andExpect(status().isNotFound());
                }
        @Test
        @WithMockUser(roles = {"ADMIN"})
        public void updateArticle_changesTitle_whenNewTitleProvided() throws Exception {
                    LocalDateTime now = LocalDateTime.now();
                    Articles originalArticle = new Articles(1L, "Original Title", "http://original.com", "Original Explanation", "admin@ucsb.edu", now);
                    Articles updatedArticleInfo = new Articles(1L, "Updated Title", "http://original.com", "Original Explanation", "admin@ucsb.edu", now);

                    when(articlesRepository.findById(1L)).thenReturn(Optional.of(originalArticle));
                    when(articlesRepository.save(any(Articles.class))).then(returnsFirstArg());

                    mockMvc.perform(put("/api/articles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(updatedArticleInfo))
                            .param("id", "1")
                            .with(csrf()))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.title", is("Updated Title")));

                    verify(articlesRepository).save(any(Articles.class)); // Verifies that save was called with updated information.
                }
        @Test
        @WithMockUser(roles = {"ADMIN"})
        public void deleteArticle_success() throws Exception {
                Long existingArticleId = 1L;
                Articles existingArticle = new Articles(existingArticleId, "Title", "http://url.com", "Explanation", "email@example.com", LocalDateTime.now());

                when(articlesRepository.findById(existingArticleId)).thenReturn(Optional.of(existingArticle));
                doNothing().when(articlesRepository).delete(any(Articles.class));

                mockMvc.perform(delete("/api/articles")
                        .param("id", existingArticleId.toString())
                        .with(csrf()))
                        .andExpect(status().isOk());

                verify(articlesRepository, times(1)).delete(any(Articles.class));
        }
}
