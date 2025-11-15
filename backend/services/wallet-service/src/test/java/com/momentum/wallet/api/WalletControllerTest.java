package com.momentum.wallet.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.wallet.api.dto.CreateWalletRequest;
import com.momentum.wallet.api.dto.WalletTransactionRequest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createWallet_thenFetchAndLedger() throws Exception {
        UUID userId = UUID.randomUUID();
        CreateWalletRequest request = new CreateWalletRequest(userId, "user@test.com", "USD", 500L);

        String response = mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", equalTo(userId.toString())))
                .andExpect(jsonPath("$.balanceMinorUnits", equalTo(500)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        WalletResponseBody created = objectMapper.readValue(response, WalletResponseBody.class);

        mockMvc.perform(get("/api/v1/wallets/{walletId}", created.walletId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId", equalTo(created.walletId().toString())))
                .andExpect(jsonPath("$.currency", equalTo("USD")));

        mockMvc.perform(get("/api/v1/wallets/{walletId}/ledger", created.walletId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void creditAndDebitEndpointsUpdateLedger() throws Exception {
        WalletResponseBody wallet = createWallet();

        WalletTransactionRequest credit = new WalletTransactionRequest(200L, "credit-req", "bonus", null);
        mockMvc.perform(post("/api/v1/wallets/{walletId}/credit", wallet.walletId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amountMinorUnits", equalTo(200)))
                .andExpect(jsonPath("$.reference", equalTo("credit-req")));

        WalletTransactionRequest debit = new WalletTransactionRequest(50L, "debit-req", "purchase", null);
        mockMvc.perform(post("/api/v1/wallets/{walletId}/debit", wallet.walletId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(debit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amountMinorUnits", equalTo(50)))
                .andExpect(jsonPath("$.reference", equalTo("debit-req")));

        mockMvc.perform(get("/api/v1/wallets/{walletId}/ledger", wallet.walletId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/v1/wallets/{walletId}", wallet.walletId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceMinorUnits", equalTo(150)));
    }

    @Test
    void debitReturnsValidationErrorWhenOverdraft() throws Exception {
        WalletResponseBody wallet = createWallet();

        WalletTransactionRequest debit = new WalletTransactionRequest(10_000L, "overdraft", null, null);
        mockMvc.perform(post("/api/v1/wallets/{walletId}/debit", wallet.walletId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(debit)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient funds for wallet " + wallet.walletId()));
    }

    private WalletResponseBody createWallet() throws Exception {
        UUID userId = UUID.randomUUID();
        CreateWalletRequest request = new CreateWalletRequest(userId, "api-" + userId + "@test.com", "USD", 0L);
        String response = mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(response, WalletResponseBody.class);
    }

    private record WalletResponseBody(UUID walletId, UUID userId, String currency, long balanceMinorUnits) {}
}
