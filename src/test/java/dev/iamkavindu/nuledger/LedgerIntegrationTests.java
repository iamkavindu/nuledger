package dev.iamkavindu.nuledger;

import static dev.iamkavindu.nuledger.LedgerTestSupport.API;
import static dev.iamkavindu.nuledger.LedgerTestSupport.PROBLEM_BASE;
import static dev.iamkavindu.nuledger.LedgerTestSupport.bearer;
import static dev.iamkavindu.nuledger.LedgerTestSupport.createAccount;
import static dev.iamkavindu.nuledger.LedgerTestSupport.createLiabilityAccount;
import static dev.iamkavindu.nuledger.LedgerTestSupport.postTransaction;
import static dev.iamkavindu.nuledger.LedgerTestSupport.reverseTransaction;
import static dev.iamkavindu.nuledger.LedgerTestSupport.uniqueCode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@Import({TestcontainersConfiguration.class, TestJwtConfig.class})
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LedgerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void tenantIsolation_hidesOtherTenantsAccounts() throws Exception {
        var code = uniqueCode("cash");
        var accountId = createAccount(mockMvc, objectMapper, "tenant-a", code);

        mockMvc.perform(get(API + "/accounts/{id}", accountId).header(HttpHeaders.AUTHORIZATION, bearer("tenant-a")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code));

        mockMvc.perform(get(API + "/accounts/{id}", accountId).header(HttpHeaders.AUTHORIZATION, bearer("tenant-b")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value(PROBLEM_BASE + "account-not-found"));

        mockMvc.perform(get(API + "/accounts")
                        .param("code", code)
                        .header(HttpHeaders.AUTHORIZATION, bearer("tenant-b")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value(PROBLEM_BASE + "account-not-found"));
    }

    @Test
    void postTransaction_isIdempotent() throws Exception {
        var cash = createAccount(mockMvc, objectMapper, "tenant-a", uniqueCode("cash"));
        var payable = createLiabilityAccount(mockMvc, objectMapper, "tenant-a", uniqueCode("payable"));
        var key = "idem-" + UUID.randomUUID();

        var first = postTransaction(mockMvc, objectMapper, "tenant-a", key, cash, payable, 10_000L);
        var entryId = first.get("entryId").asString();

        mockMvc.perform(post(API + "/transactions")
                        .header(HttpHeaders.AUTHORIZATION, bearer("tenant-a"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "idempotencyKey": "%s",
                          "lines": [
                            {"accountId": "%s", "direction": "DEBIT", "amountMinor": 10000, "currency": "LKR"},
                            {"accountId": "%s", "direction": "CREDIT", "amountMinor": 10000, "currency": "LKR"}
                          ]
                        }
                        """.formatted(key, cash, payable)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entryId").value(entryId))
                .andExpect(jsonPath("$.replayed").value(true));
    }

    @Test
    void postTransaction_rejectsUnbalancedEntry() throws Exception {
        var cash = createAccount(mockMvc, objectMapper, "tenant-a", uniqueCode("cash"));
        var payable = createLiabilityAccount(mockMvc, objectMapper, "tenant-a", uniqueCode("payable"));

        mockMvc.perform(post(API + "/transactions")
                        .header(HttpHeaders.AUTHORIZATION, bearer("tenant-a"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "idempotencyKey": "unbalanced-%s",
                          "lines": [
                            {"accountId": "%s", "direction": "DEBIT", "amountMinor": 10000, "currency": "LKR"},
                            {"accountId": "%s", "direction": "CREDIT", "amountMinor": 5000, "currency": "LKR"}
                          ]
                        }
                        """.formatted(UUID.randomUUID(), cash, payable)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.type").value(PROBLEM_BASE + "invalid-posting"))
                .andExpect(jsonPath("$.detail").value("Debits and credits must balance for a single currency"));
    }

    @Test
    void reverseTransaction_createsContraAndIsIdempotent() throws Exception {
        var cash = createAccount(mockMvc, objectMapper, "tenant-a", uniqueCode("cash"));
        var payable = createLiabilityAccount(mockMvc, objectMapper, "tenant-a", uniqueCode("payable"));
        var txKey = "tx-" + UUID.randomUUID();

        var posted = postTransaction(mockMvc, objectMapper, "tenant-a", txKey, cash, payable, 10_000L);
        var entryId = UUID.fromString(posted.get("entryId").asString());

        mockMvc.perform(get(API + "/accounts/{id}/balances", cash)
                        .header(HttpHeaders.AUTHORIZATION, bearer("tenant-a")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balances[0].amountMinor").value(10_000));
        var revKey = "rev-" + UUID.randomUUID();

        reverseTransaction(mockMvc, "tenant-a", entryId, revKey)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalEntryId").value(entryId.toString()))
                .andExpect(jsonPath("$.replayed").value(false));

        mockMvc.perform(get(API + "/accounts/{id}/balances", cash)
                        .header(HttpHeaders.AUTHORIZATION, bearer("tenant-a")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balances[0].amountMinor").value(0));

        reverseTransaction(mockMvc, "tenant-a", entryId, revKey)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.replayed").value(true));

        reverseTransaction(mockMvc, "tenant-a", entryId, "rev-duplicate-" + UUID.randomUUID())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value(PROBLEM_BASE + "entry-already-reversed"));
    }
}
