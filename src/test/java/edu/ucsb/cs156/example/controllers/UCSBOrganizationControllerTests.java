package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.controllers.UCSBOrganizationController;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

import java.beans.Transient;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

    @MockBean
    UCSBOrganizationRepository ucsbOrganizationRepository;

    @MockBean
    UserRepository userRepository;


    // GET
    @Test
    public void allOrganizations__logged_out() throws Exception {
        mockMvc.perform(get("/api/ucsborganization/all"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void allOrganizations__logged_in() throws Exception {
        // arrange
        UCSBOrganization org1 = new UCSBOrganization();
        org1.setOrgCode("org1");
        org1.setOrgTranslationShort("org1");
        org1.setOrgTranslation("org1");
        org1.setInactive(false);

        UCSBOrganization org2 = new UCSBOrganization();
        org2.setOrgCode("org2");
        org2.setOrgTranslationShort("org2");
        org2.setOrgTranslation("org2");
        org2.setInactive(false);

        when(ucsbOrganizationRepository.findAll()).thenReturn(Arrays.asList(org1, org2));

        String expectedJson = mapper.writeValueAsString(Arrays.asList(org1, org2));

        // act
        MvcResult response = mockMvc.perform(get("/api/ucsborganization/all"))
            .andExpect(status().isOk()).andReturn();

        // assert
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @Test
    public void getOrganization__logged_out() throws Exception {
        mockMvc.perform(get("/api/ucsborganization?id=org1"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void getOrganization__logged_in() throws Exception {
        // arrange
        UCSBOrganization org1 = new UCSBOrganization();
        org1.setOrgCode("org1");
        org1.setOrgTranslationShort("org1");
        org1.setOrgTranslation("org1");
        org1.setInactive(false);

        when(ucsbOrganizationRepository.findById("org1")).thenReturn(Optional.of(org1));

        String expectedJson = mapper.writeValueAsString(org1);

        MvcResult response = mockMvc.perform(get("/api/ucsborganization?id=org1"))
            .andExpect(status().isOk()).andReturn();

        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void getOrganization__logged_in__not_found() throws Exception {
        // arrange
        when(ucsbOrganizationRepository.findById("org1")).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(get("/api/ucsborganization?id=org1"))
            .andExpect(status().is(404)).andReturn();
    }

    // POST

    @Test
    public void postOrganization__logged_out() throws Exception {
        mockMvc.perform(post("/api/ucsborganization/post"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void postOrganization__user() throws Exception {
        mockMvc.perform(post("/api/ucsborganization/post"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "ADMIN" })
    @Test
    public void postOrganization__admin() throws Exception {
        UCSBOrganization org1 = UCSBOrganization.builder()
            .orgCode("org1")
            .orgTranslationShort("org1")
            .orgTranslation("org1")
            .inactive(true)
            .build();

        when(ucsbOrganizationRepository.save(eq(org1))).thenReturn(org1);

        MvcResult response = mockMvc.perform(
            post("/api/ucsborganization/post?orgCode=org1&orgTranslationShort=org1&orgTranslation=org1&inactive=true")
            .with(csrf()))
            .andExpect(status().isOk()).andReturn();

        verify(ucsbOrganizationRepository, times(1)).save(org1);
        String expectedJson = mapper.writeValueAsString(org1);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // PUT

    @Test
    public void updateOrganization__logged_out() throws Exception {
        mockMvc.perform(put("/api/ucsborganization"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void updateOrganization__user() throws Exception {
        mockMvc.perform(put("/api/ucsborganization"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void updateOrganization__admin() throws Exception {
        UCSBOrganization org1 = UCSBOrganization.builder()
            .orgCode("org1")
            .orgTranslationShort("org1")
            .orgTranslation("org1")
            .inactive(true)
            .build();
        
        UCSBOrganization updatedOrg1 = UCSBOrganization.builder()
            .orgCode("org1")
            .orgTranslationShort("org2")
            .orgTranslation("org2")
            .inactive(false)
            .build();

        String requestBody = mapper.writeValueAsString(updatedOrg1);

        when(ucsbOrganizationRepository.findById(eq("org1")))
            .thenReturn(Optional.of(org1));

        when(ucsbOrganizationRepository.save(eq(org1))).thenReturn(org1);

        MvcResult response = mockMvc.perform(
            put("/api/ucsborganization?id=org1")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(requestBody)
            .with(csrf()))
            .andExpect(status().isOk()).andReturn();

        verify(ucsbOrganizationRepository, times(1)).findById("org1");
        verify(ucsbOrganizationRepository, times(1)).save(org1);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void updateOrganization__admin__not_found() throws Exception {
        UCSBOrganization updatedOrg1 = UCSBOrganization.builder()
            .orgCode("org1")
            .orgTranslationShort("org2")
            .orgTranslation("org2")
            .inactive(false)
            .build();
        
        String requestBody = mapper.writeValueAsString(updatedOrg1);

        when(ucsbOrganizationRepository.findById(eq("org1")))
            .thenReturn(Optional.empty());
        
        MvcResult response = mockMvc.perform(
            put("/api/ucsborganization?id=org1")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(requestBody)
            .with(csrf()))
            .andExpect(status().isNotFound()).andReturn();
        
        verify(ucsbOrganizationRepository, times(1)).findById("org1");
        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrganization with id org1 not found", json.get("message"));
    }

    // DELETE

    @Test
    public void deleteOrganization__logged_out() throws Exception {
        mockMvc.perform(delete("/api/ucsborganization"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void deleteOrganization__user() throws Exception {
        mockMvc.perform(delete("/api/ucsborganization"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void deleteOrganization__admin() throws Exception {

        UCSBOrganization org1 = UCSBOrganization.builder()
            .orgCode("org1")
            .orgTranslationShort("org1")
            .orgTranslation("org1")
            .inactive(true)
            .build();

        when(ucsbOrganizationRepository.findById(eq("org1")))
            .thenReturn(Optional.of(org1));

        MvcResult response = mockMvc.perform(
            delete("/api/ucsborganization?id=org1")
            .with(csrf()))
            .andExpect(status().isOk()).andReturn();
        
        verify(ucsbOrganizationRepository, times(1)).findById("org1");
        verify(ucsbOrganizationRepository, times(1)).delete(any());

        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrganization with id org1 deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void deleteOrganization__admin__not_found() throws Exception {
        when(ucsbOrganizationRepository.findById(eq("org1")))
            .thenReturn(Optional.empty());
        
        MvcResult response = mockMvc.perform(
            delete("/api/ucsborganization?id=org1")
            .with(csrf()))
            .andExpect(status().isNotFound()).andReturn();
        
        verify(ucsbOrganizationRepository, times(1)).findById("org1");
        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrganization with id org1 not found", json.get("message"));
    }

    
}

