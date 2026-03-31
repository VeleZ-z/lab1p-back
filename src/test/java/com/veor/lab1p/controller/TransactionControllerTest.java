package com.veor.lab1p.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veor.lab1p.dto.TransactionDTO;
import com.veor.lab1p.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void shouldPostTransactionSuccessfully() throws Exception {
        TransactionDTO request = new TransactionDTO();
        request.setSenderAccountNumber("ACC-001");
        request.setReceiverAccountNumber("ACC-002");
        request.setAmount(250.0);
        request.setTimestamp(LocalDateTime.of(2026, 3, 31, 16, 0));

        TransactionDTO response = new TransactionDTO();
        response.setId(10L);
        response.setSenderAccountNumber("ACC-001");
        response.setReceiverAccountNumber("ACC-002");
        response.setAmount(250.0);
        response.setTimestamp(LocalDateTime.of(2026, 3, 31, 16, 0));

        when(transactionService.transferMoney(any(TransactionDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.senderAccountNumber").value("ACC-001"))
                .andExpect(jsonPath("$.receiverAccountNumber").value("ACC-002"))
                .andExpect(jsonPath("$.amount").value(250.0))
                .andExpect(jsonPath("$.timestamp").value("2026-03-31T16:00:00"));
    }

    @Test
    void shouldReturnBadRequestWhenTransactionFails() throws Exception {
        TransactionDTO request = new TransactionDTO();
        request.setSenderAccountNumber("ACC-001");
        request.setReceiverAccountNumber("ACC-002");
        request.setAmount(250.0);

        when(transactionService.transferMoney(any(TransactionDTO.class)))
                .thenThrow(new IllegalArgumentException("Saldo insuficiente en la cuenta del remitente."));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Saldo insuficiente en la cuenta del remitente."));
    }

    @Test
    void shouldGetTransactionsByAccount() throws Exception {
        TransactionDTO transaction = new TransactionDTO();
        transaction.setId(10L);
        transaction.setSenderAccountNumber("ACC-001");
        transaction.setReceiverAccountNumber("ACC-002");
        transaction.setAmount(250.0);
        transaction.setTimestamp(LocalDateTime.of(2026, 3, 31, 16, 0));

        when(transactionService.getTransactionsForAccount("ACC-001")).thenReturn(List.of(transaction));

        mockMvc.perform(get("/api/transactions/ACC-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].senderAccountNumber").value("ACC-001"))
                .andExpect(jsonPath("$[0].receiverAccountNumber").value("ACC-002"))
                .andExpect(jsonPath("$[0].amount").value(250.0))
                .andExpect(jsonPath("$[0].timestamp").value("2026-03-31T16:00:00"));
    }
}
