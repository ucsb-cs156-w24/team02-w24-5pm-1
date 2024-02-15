package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
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

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

        @MockBean
        RecommendationRequestRepository RecommendationRequestRepository;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/RecommendationRequest/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/RecommendationRequest/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/RecommendationRequest/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_recommendationrequests() throws Exception {
                          // arrange
                          LocalDateTime time1 = LocalDateTime.parse("2022-01-03T00:00:00");
                          LocalDateTime time2 = LocalDateTime.parse("2022-02-03T00:00:00");
          
                          RecommendationRequest RecommendationRequest1 = RecommendationRequest.builder()
                                          .requesterEmail("student@ucsb.edu")
                                          .professorEmail("prof@ucsb.edu")
                                          .explanation("Letter of Recommendation")
                                          .dateRequested(time1)
                                          .dateNeeded(time2)
                                          .done(true)
                                          .build();
          
                          LocalDateTime time12 = LocalDateTime.parse("2022-03-11T00:00:00");
                          LocalDateTime time22 = LocalDateTime.parse("2022-04-11T00:00:00");
          
                          RecommendationRequest RecommendationRequest2 = RecommendationRequest.builder()
                                          .requesterEmail("student2@ucsb.edu")
                                          .professorEmail("prof2@ucsb.edu")
                                          .explanation("Letter of Recommendation number 2")
                                          .dateRequested(time12)
                                          .dateNeeded(time22)
                                          .done(true)
                                          .build();
          
                          ArrayList<RecommendationRequest> expectedRequests = new ArrayList<>();
                          expectedRequests.addAll(Arrays.asList(RecommendationRequest1, RecommendationRequest2));
          
                          when(RecommendationRequestRepository.findAll()).thenReturn(expectedRequests);
          
                          // act
                          MvcResult response = mockMvc.perform(get("/api/RecommendationRequest/all"))
                                          .andExpect(status().isOk()).andReturn();
          
                          // assert
          
                          verify(RecommendationRequestRepository, times(1)).findAll();
                          String expectedJson = mapper.writeValueAsString(expectedRequests);
                          String responseString = response.getResponse().getContentAsString();
                          assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/RecommendationRequest/post...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/RecommendationRequest/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/RecommendationRequest/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_recommendationrequest() throws Exception {
                // arrange

                LocalDateTime ldt11 = LocalDateTime.parse("2022-01-03T00:00:00");

                LocalDateTime ldt12 = LocalDateTime.parse("2022-02-04T00:00:00");

                RecommendationRequest RecReq1 = RecommendationRequest.builder()
                                .requesterEmail("student@ucsb.edu")
                                .professorEmail("prof@ucsb.edu")
                                .explanation("Letter for Recommendation")
                                .dateRequested(ldt11)
                                .dateNeeded(ldt12)
                                .done(true)
                                .build();

                when(RecommendationRequestRepository.save(eq(RecReq1))).thenReturn(RecReq1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/RecommendationRequest/post?requesterEmail=student@ucsb.edu&professorEmail=prof@ucsb.edu&explanation=Letter for Recommendation&dateRequested=2022-01-03T00:00:00&dateNeeded=2022-02-04T00:00:00&done=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(RecommendationRequestRepository, times(1)).save(RecReq1);
                String expectedJson = mapper.writeValueAsString(RecReq1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/RecommendationRequest?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2022-01-04T00:00:00");
                RecommendationRequest RecRequest = RecommendationRequest.builder()
                    .requesterEmail("student@ucsb.edu")
                    .professorEmail("prof@ucsb.edu")
                    .explanation("Letter for Recommendation")
                    .dateRequested(ldt1)
                    .dateNeeded(ldt2)
                    .done(true)
                    .build();

                when(RecommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.of(RecRequest));

                // act
                MvcResult response = mockMvc.perform(get("/api/RecommendationRequest?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(RecommendationRequestRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(RecRequest);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(RecommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/RecommendationRequest?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(RecommendationRequestRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
        }

        // DELETE
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_request() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt12 = LocalDateTime.parse("2022-02-03T00:00:00");

                RecommendationRequest RecommendationRequest1 = RecommendationRequest.builder()
                                .requesterEmail("student@ucsb.edu")
                                .professorEmail("prof@ucsb.edu")
                                .explanation("letter of Recommendation")
                                .dateRequested(ldt1)
                                .dateNeeded(ldt12)
                                .done(false)
                                .build();

                when(RecommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.of(RecommendationRequest1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/RecommendationRequest?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(RecommendationRequestRepository, times(1)).findById(15L);
                verify(RecommendationRequestRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("RecommendationRequest with id 15 deleted", json.get("message"));
        }
        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_recommendationrequest_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(RecommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/RecommendationRequest?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(RecommendationRequestRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("RecommendationRequest with id 15 not found", json.get("message"));
        }


        // put
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_recommendationrequest() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

                RecommendationRequest RecommendationRequestOrig = RecommendationRequest.builder()
                                .requesterEmail("student@ucsb.edu")
                                .professorEmail("professor@ucsb.edu")
                                .explanation("Letter of Rec")
                                .dateRequested(ldt1)
                                .dateNeeded(ldt1)
                                .done(false)
                                .build();

                RecommendationRequest RecommendationRequestEdited = RecommendationRequest.builder()
                                .requesterEmail("student2@ucsb.edu")
                                .professorEmail("professor2@ucsb.edu")
                                .explanation("Second Letter of Rec")
                                .dateRequested(ldt2)
                                .dateNeeded(ldt2)
                                .done(true)
                                .build();

                String requestBody = mapper.writeValueAsString(RecommendationRequestEdited);

                when(RecommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.of(RecommendationRequestOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/RecommendationRequest?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(RecommendationRequestRepository, times(1)).findById(67L);
                verify(RecommendationRequestRepository, times(1)).save(RecommendationRequestEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_recommendationrequest_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt21 = LocalDateTime.parse("2022-01-04T00:00:00");
                LocalDateTime ldt22 = LocalDateTime.parse("2023-01-05T00:00:00");
                RecommendationRequest RecRequestEdited = RecommendationRequest.builder()
                    .requesterEmail("student2@ucsb.edu")
                    .professorEmail("prof2@ucsb.edu")
                    .explanation("Letter for Recommendation")
                    .dateRequested(ldt21)
                    .dateNeeded(ldt22)
                    .done(true)
                    .build();

                String requestBody = mapper.writeValueAsString(RecRequestEdited);

                when(RecommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/RecommendationRequest?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(RecommendationRequestRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("RecommendationRequest with id 67 not found", json.get("message"));

        }

}