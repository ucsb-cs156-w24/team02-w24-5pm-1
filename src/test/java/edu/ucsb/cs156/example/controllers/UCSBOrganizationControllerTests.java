package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.controllers.UCSBOrganizationController;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

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
}

