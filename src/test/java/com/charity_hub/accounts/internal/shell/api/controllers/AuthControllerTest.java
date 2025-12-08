package com.charity_hub.accounts.internal.shell.api.controllers;

import com.charity_hub.accounts.internal.core.commands.Authenticate.Authenticate;
import com.charity_hub.accounts.internal.core.commands.Authenticate.AuthenticateHandler;
import com.charity_hub.accounts.internal.core.commands.Authenticate.AuthenticateResponse;
import com.charity_hub.shared.exceptions.GlobalExceptionHandler;
import com.charity_hub.shared.observability.TestObservabilityConfiguration;
import com.charity_hub.shared.observability.metrics.BusinessMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, TestObservabilityConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticateHandler authenticateHandler;

    @MockBean
    private BusinessMetrics businessMetrics;

    @Test
    @DisplayName("Should authenticate user and return tokens")
    void shouldAuthenticateUserAndReturnTokens() throws Exception {
        Authenticate request = new Authenticate(
                "firebase-token-123",
                "device-123456789012345",
                "ANDROID"
        );
        
        AuthenticateResponse response = new AuthenticateResponse(
                "access-token",
                "refresh-token"
        );
        
        when(authenticateHandler.handle(any(Authenticate.class))).thenReturn(response);

        mockMvc.perform(post("/v1/accounts/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authenticateHandler).handle(any(Authenticate.class));
    }

    @Test
    @DisplayName("Should return tokens for any valid authentication request")
    void shouldReturnTokensForValidRequest() throws Exception {
        Authenticate request = new Authenticate(
                "firebase-token-123",
                "device-123456789012345",
                "ANDROID"
        );
        
        AuthenticateResponse response = new AuthenticateResponse(
                "access-token",
                "refresh-token"
        );
        
        when(authenticateHandler.handle(any(Authenticate.class))).thenReturn(response);

        mockMvc.perform(post("/v1/accounts/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }
}
