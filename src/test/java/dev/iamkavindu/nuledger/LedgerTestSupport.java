package dev.iamkavindu.nuledger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

final class LedgerTestSupport {

    static final String API = "/api/v1";
    static final String PROBLEM_BASE = "https://nuledger.dev/problems/";

    private LedgerTestSupport() {}

    static String bearer(String tenantId) {
        return "Bearer " + tenantId;
    }

    static String uniqueCode(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    static UUID createAccount(MockMvc mockMvc, ObjectMapper objectMapper, String tenant, String code) throws Exception {

        var body = """
                {
                  "code": "%s",
                  "name": "Test account",
                  "accountType": "ASSET",
                  "allowNegative": false
                }
                """.formatted(code);

        var json = mockMvc.perform(post(API + "/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tenant))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(json).get("id").asString());
    }

    static UUID createLiabilityAccount(MockMvc mockMvc, ObjectMapper objectMapper, String tenant, String code)
            throws Exception {

        var body = """
                {
                  "code": "%s",
                  "name": "Test liability",
                  "accountType": "LIABILITY",
                  "allowNegative": false
                }
                """.formatted(code);

        var json = mockMvc.perform(post(API + "/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tenant))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(json).get("id").asString());
    }

    static JsonNode postTransaction(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            String tenant,
            String idempotencyKey,
            UUID debitAccountId,
            UUID creditAccountId,
            long amountMinor)
            throws Exception {

        var body = """
                {
                  "idempotencyKey": "%s",
                  "lines": [
                    {
                      "accountId": "%s",
                      "direction": "DEBIT",
                      "amountMinor": %d,
                      "currency": "LKR"
                    },
                    {
                      "accountId": "%s",
                      "direction": "CREDIT",
                      "amountMinor": %d,
                      "currency": "LKR"
                    }
                  ]
                }
                """.formatted(idempotencyKey, debitAccountId, amountMinor, creditAccountId, amountMinor);

        var result = mockMvc.perform(post(API + "/transactions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tenant))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    static ResultActions reverseTransaction(MockMvc mockMvc, String tenant, UUID entryId, String idempotencyKey)
            throws Exception {

        var body = """
                {"idempotencyKey": "%s"}
                """.formatted(idempotencyKey);

        return mockMvc.perform(post(API + "/transactions/{entryId}/reverse", entryId)
                .header(HttpHeaders.AUTHORIZATION, bearer(tenant))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
