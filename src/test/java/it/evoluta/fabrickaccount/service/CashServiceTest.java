package it.evoluta.fabrickaccount.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.evoluta.fabrickaccount.config.CashProperties;
import it.evoluta.fabrickaccount.exception.NoPayloadReceivedException;
import it.evoluta.fabrickaccount.exception.NoTransactionsFoundException;
import it.evoluta.fabrickaccount.model.ApiResponse;
import it.evoluta.fabrickaccount.model.BalancePayload;
import it.evoluta.fabrickaccount.model.BonificoInput;
import it.evoluta.fabrickaccount.model.TransactionAccountDTO;
import it.evoluta.fabrickaccount.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CashServiceTest {

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
    private CashService cashService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CashProperties cashProperties;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(cashProperties.getSchema()).thenReturn("S2S");
        when(cashProperties.getKey()).thenReturn("API-KEY");
        when(cashProperties.getBaseUrl()).thenReturn("https://sandbox.platfr.io");
    }

    @Test
    void testAccountBalance_Success() throws JsonProcessingException, NoPayloadReceivedException {
        Long accountId = 14537780L;
        BalancePayload expectedPayload = new BalancePayload();
        expectedPayload.setDate("2023-08-30");
        expectedPayload.setBalance(-7.01);
        expectedPayload.setAvailableBalance(-7.01);
        expectedPayload.setCurrency("EUR");

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setPayload(expectedPayload);

        when(restTemplate.exchange(anyString(), Mockito.any(), Mockito.any(HttpEntity.class), eq(ApiResponse.class)))
                .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));

        BalancePayload actualPayload = cashService.accountBalance(accountId);
        assertEquals(expectedPayload, actualPayload);
    }
    @Test
    void testAccountBalance_NoPayload() {
        Long accountId = 14537780L;

        when(restTemplate.exchange(anyString(), Mockito.any(), Mockito.any(HttpEntity.class), eq(ApiResponse.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        NoPayloadReceivedException exception= assertThrows(NoPayloadReceivedException.class, () -> {
            cashService.accountBalance(accountId);
        });
        assertNotNull(exception);
        assertEquals("No payload received for account ID: 14537780", exception.getMessage());
    }

    @Test
    void testAccountBalance_RestClientException() {
        Long accountId = 14537780L;

        when(restTemplate.exchange(anyString(), Mockito.any(), Mockito.any(HttpEntity.class), eq(ApiResponse.class)))
                .thenThrow(new RestClientException("403 Forbidden: \"{<EOL><EOL>  \"status\" : \"KO\",<EOL><EOL>  \"errors\" : [{\"code\":\"REQ004\",\"description\":\"Invalid account identifier\",\"params\":\"\"}],<EOL><EOL>  \"payload\": {}<EOL><EOL>}"));

        assertThrows(ClassCastException.class, () -> {
            cashService.accountBalance(accountId);
        });
    }

    @Test
    void testGetTransactions_Success() throws Exception {
        String accountId = "14537780";
        String fromAccountingDate = "2021-01-01";
        String toAccountingDate = "2021-01-31";

        ResponseEntity<String> mockedResponse = new ResponseEntity<>("{\"payload\": {\"list\": [{\"transactionId\": \"1\", \"accountingDate\": \"2019-04-01\",\n" +
                "      \"valueDate\": \"2019-04-01\"}]}}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), Mockito.any(), Mockito.any(), eq(String.class)))
                .thenReturn(mockedResponse);

        // Run the method under test
        List<TransactionAccountDTO> transactions = cashService.getTransactions(accountId, fromAccountingDate, toAccountingDate);

        // Validate the results
        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals("1", transactions.get(0).getTransactionId());

    }

    @Test
    void testGetTransactions_NoTransactionsFound() {
        String accountId = "14537780";
        String fromAccountingDate = "2021-01-01";
        String toAccountingDate = "2021-01-31";

        ResponseEntity<String> mockedResponse = new ResponseEntity<>("{\"payload\": {\"list\": []}}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), Mockito.any(), Mockito.any(), eq(String.class)))
                .thenReturn(mockedResponse);

        assertThrows(NoTransactionsFoundException.class, () -> {
            cashService.getTransactions(accountId, fromAccountingDate, toAccountingDate);
        });
    }

    @Test
    void testEffettuaBonifico_Success() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        BonificoInput input = mapper.readValue(inputJson, BonificoInput.class);
        input.setAmount(800);
        String accountId = "74586220";

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), Mockito.any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("Successful response", HttpStatus.OK));

        String result = cashService.effettuaBonifico(input, accountId);

        assertEquals("Successful response", result);
    }

    @Test
    void testEffettuaBonifico_ClientError() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        BonificoInput input = mapper.readValue(inputJson, BonificoInput.class);
        String accountId = "1425877";

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), Mockito.any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));

        String result = cashService.effettuaBonifico(input, accountId);

        assertTrue(result.contains("Errore client"));
    }

    @Test
    void testEffettuaBonifico_ServerError() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        BonificoInput input = mapper.readValue(inputJson, BonificoInput.class);
        String accountId = "124596";

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), Mockito.any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));

        String result = cashService.effettuaBonifico(input, accountId);

        assertTrue(result.contains("Errore server"));
    }
}