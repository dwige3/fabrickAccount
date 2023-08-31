package it.evoluta.fabrickaccount.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.evoluta.fabrickaccount.model.BalancePayload;
import it.evoluta.fabrickaccount.model.BonificoInput;
import it.evoluta.fabrickaccount.model.TransactionAccountDTO;
import it.evoluta.fabrickaccount.service.CashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CashControllerTest {

    private static String inputJson = "{\n" +
            "  \"creditor\": {\n" +
            "    \"name\": \"John Doe\",\n" +
            "    \"account\": {\n" +
            "      \"accountCode\": \"IT23A0336844430152923804660\",\n" +
            "      \"bicCode\": \"SELBIT2BXXX\"\n" +
            "    },\n" +
            "    \"address\": {\n" +
            "      \"address\": null,\n" +
            "      \"city\": null,\n" +
            "      \"countryCode\": null\n" +
            "    }\n" +
            "  },\n" +
            "  \"executionDate\": \"2019-04-01\",\n" +
            "  \"uri\": \"REMITTANCE_INFORMATION\",\n" +
            "  \"description\": \"Payment invoice 75/2017\",\n" +
            "  \"amount\": 800,\n" +
            "  \"currency\": \"EUR\",\n" +
            "  \"isUrgent\": false,\n" +
            "  \"isInstant\": false,\n" +
            "  \"feeType\": \"SHA\",\n" +
            "  \"feeAccountId\": \"45685475\",\n" +
            "  \"taxRelief\": {\n" +
            "    \"taxReliefId\": \"L449\",\n" +
            "    \"isCondoUpgrade\": false,\n" +
            "    \"creditorFiscalCode\": \"56258745832\",\n" +
            "    \"beneficiaryType\": \"NATURAL_PERSON\",\n" +
            "    \"naturalPersonBeneficiary\": {\n" +
            "      \"fiscalCode1\": \"MRLFNC81L04A859L\",\n" +
            "      \"fiscalCode2\": null,\n" +
            "      \"fiscalCode3\": null,\n" +
            "      \"fiscalCode4\": null,\n" +
            "      \"fiscalCode5\": null\n" +
            "    },\n" +
            "    \"legalPersonBeneficiary\": {\n" +
            "      \"fiscalCode\": null,\n" +
            "      \"legalRepresentativeFiscalCode\": null\n" +
            "    }\n" +
            "  }\n" +
            "}";

    @InjectMocks
    private CashController cashController;

    private MockMvc mockMvc;

    @Mock
    private CashService cashService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(cashController).build();
    }

    @Test
    void testGetBalance() throws Exception {
        Long accountId = 14537780L;
        BalancePayload mockBalance = new BalancePayload();
        mockBalance.setDate("2023-08-30");
        mockBalance.setBalance(-7.01);
        mockBalance.setAvailableBalance(-7.01);
        mockBalance.setCurrency("EUR");

        when(cashService.accountBalance(accountId)).thenReturn(mockBalance);

        mockMvc.perform(get("/api/accounts/{accountId}/balance", accountId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2023-08-30"))
                .andExpect(jsonPath("$.balance").value(-7.01))
                .andExpect(jsonPath("$.availableBalance").value(-7.01))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void testEffettuaBonifico() throws Exception {
        String accountId = "14537780";
        String response = "success";

        when(cashService.effettuaBonifico(Mockito.any(), Mockito.anyString())).thenReturn(response);

        mockMvc.perform(post("/api/accounts/{accountId}/payments/money-transfers", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inputJson))
                .andExpect(status().isOk())
                .andExpect(content().string(response));
    }

    @Test
    void testGetTransactions() throws Exception {
        String accountId = "14537780";
        String fromAccountingDate = "2021-01-01";
        String toAccountingDate = "2021-12-31";

        TransactionAccountDTO transaction1 = new TransactionAccountDTO();
        transaction1.setTransactionId("1331714087");
        transaction1.setOperationId("00000000273015");
        transaction1.setAccountingDate(new SimpleDateFormat("yyyy-MM-dd").parse("2019-04-01"));
        transaction1.setValueDate(new SimpleDateFormat("yyyy-MM-dd").parse("2019-04-01"));
        transaction1.setTransactionType("GBS_TRANSACTION_TYPE_0023");
        transaction1.setAmount(-800.0);
        transaction1.setCurrency("EUR");
        transaction1.setDescription("BA JOHN DOE PAYMENT INVOICE 75/2017");

        TransactionAccountDTO transaction2 = new TransactionAccountDTO();
        transaction2.setTransactionId("1331714088");
        transaction2.setOperationId("00000000273016");
        transaction2.setAccountingDate(new SimpleDateFormat("yyyy-MM-dd").parse("2019-05-01"));
        transaction2.setValueDate(new SimpleDateFormat("yyyy-MM-dd").parse("2019-05-01"));
        transaction2.setTransactionType("GBS_TRANSACTION_TYPE_0015");
        transaction2.setAmount(-1.0);
        transaction2.setCurrency("EUR");
        transaction2.setDescription("CO MONEY TRANSFER FEES");

        List<TransactionAccountDTO> transactions = Arrays.asList(transaction1, transaction2);

        when(cashService.getTransactions(accountId, fromAccountingDate, toAccountingDate)).thenReturn(transactions);


        mockMvc.perform(get("/api/accounts/{accountId}/transactions", accountId)
                        .param("fromAccountingDate", fromAccountingDate)
                        .param("toAccountingDate", toAccountingDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(transactions)));
    }

}
