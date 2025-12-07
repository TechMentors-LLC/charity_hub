package com.charity_hub.accounts.internal.shell.api.controllers;

import com.charity_hub.accounts.internal.core.commands.BlockAccount.BlockAccountHandler;
import com.charity_hub.shared.exceptions.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BlockAccountController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("BlockAccountController Tests")
class BlockAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlockAccountHandler blockAccountHandler;

    @Test
    @WithMockUser(authorities = "FULL_ACCESS")
    @DisplayName("Should block account successfully")
    void shouldBlockAccountSuccessfully() throws Exception {
        String userId = UUID.randomUUID().toString();
        
        doNothing().when(blockAccountHandler).handle(argThat(cmd -> 
                cmd.userId().equals(userId) && !cmd.isUnblock()));

        mockMvc.perform(post("/v1/accounts/{userId}/block", userId))
                .andExpect(status().isOk());

        verify(blockAccountHandler).handle(argThat(command ->
                command.userId().equals(userId) && !command.isUnblock()
        ));
    }
}
