package com.charity_hub.accounts.internal.api.controllers;

import com.charity_hub.accounts.internal.application.commands.ChangePermission.ChangePermissionHandler;
import com.charity_hub.accounts.internal.api.dtos.ChangePermissionRequest;
import com.charity_hub.shared.exceptions.GlobalExceptionHandler;
import com.charity_hub.shared.observability.TestObservabilityConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddUserPermissionController.class)
@Import({ GlobalExceptionHandler.class, TestObservabilityConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AddUserPermissionController Tests")
class AddUserPermissionControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ChangePermissionHandler changePermissionHandler;

        @Test
        @WithMockUser(authorities = "FULL_ACCESS")
        @DisplayName("Should add permission to user successfully")
        void shouldAddPermissionToUserSuccessfully() throws Exception {
                UUID userId = UUID.randomUUID();
                ChangePermissionRequest request = new ChangePermissionRequest("CREATE_CASES");

                doNothing().when(changePermissionHandler).handle(argThat(cmd -> cmd.userId().equals(userId) &&
                                cmd.permission().equals("CREATE_CASES") &&
                                cmd.shouldAdd()));

                mockMvc.perform(post("/v1/accounts/{userId}/permissions", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());

                verify(changePermissionHandler).handle(argThat(command -> command.userId().equals(userId) &&
                                command.permission().equals("CREATE_CASES") &&
                                command.shouldAdd()));
        }
}
