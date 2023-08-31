package it.evoluta.fabrickaccount.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.evoluta.fabrickaccount.config.CashProperties;
import it.evoluta.fabrickaccount.exception.ApiException;
import it.evoluta.fabrickaccount.exception.InvalidResponseException;
import it.evoluta.fabrickaccount.exception.NoPayloadReceivedException;
import it.evoluta.fabrickaccount.exception.NoTransactionsFoundException;
import it.evoluta.fabrickaccount.exception.UnexpectedApiException;
import it.evoluta.fabrickaccount.mappers.TransactionAccountMapper;
import it.evoluta.fabrickaccount.model.ApiResponse;
import it.evoluta.fabrickaccount.model.BalancePayload;
import it.evoluta.fabrickaccount.model.BonificoInput;
import it.evoluta.fabrickaccount.model.TransactionAccountDTO;
import it.evoluta.fabrickaccount.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class CashService {

    private static final String AUTH_SCHEMA = "Auth-Schema";

    private static final String API_KEY = "Api-Key";

    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    private final TransactionRepository transactionRepository;


    private final CashProperties cashProperties;

    private final RestTemplate restTemplate;

    public CashService(TransactionRepository transactionRepository, CashProperties cashProperties, RestTemplate restTemplate) {
        this.transactionRepository = transactionRepository;
        this.cashProperties = cashProperties;
        this.restTemplate = restTemplate;
    }

    public BalancePayload accountBalance(Long accountId) throws NoPayloadReceivedException, JsonProcessingException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(AUTH_SCHEMA, cashProperties.getSchema());
            headers.set(API_KEY, cashProperties.getKey());

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    cashProperties.getBaseUrl() + accountId + "/balance",
                    HttpMethod.GET,
                    entity,
                    ApiResponse.class
            );
            if (response.getBody() != null && response.getBody().getPayload() != null) {
                log.info("Successfully fetched balance for account ID: {}", accountId);
                return response.getBody().getPayload();
            } else {
                log.error("No payload received for account ID: {}", accountId);
                throw new NoPayloadReceivedException("No payload received for account ID: " + accountId);
            }
        } catch (RestClientException e) {
            String message = getErrorMessage(e);
            log.error("Error fetching balance for account ID: {}. Error: {}", accountId, e.getMessage());
            throw new NoPayloadReceivedException(message);
        }
    }

    public String effettuaBonifico(BonificoInput input, String accountId) {

        // Creazione dell'header con il tipo di contenuto JSON
        log.debug("Imposta l'header della richiesta con il tipo di media application/json");
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTH_SCHEMA, cashProperties.getSchema());
        headers.set(API_KEY, cashProperties.getKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // Creazione della mappa per i valori del corpo della richiesta
        Map<String, Object> requestBody = new HashMap<>();

        // Creazione dell'oggetto account
        Map<String, Object> account = new HashMap<>();
        account.put("accountCode", input.getCreditor().getAccount().accountCode);
        account.put("bicCode", input.getCreditor().getAccount().getBicCode());

        // Creazione dell'oggetto address
        Map<String, Object> address = new HashMap<>();
        address.put("address", input.getCreditor().getAddress().getAddress());
        address.put("city", input.getCreditor().getAddress().getCity());
        address.put("countryCode", input.getCreditor().getAddress().getCountryCode());

        // Creazione dell'oggetto creditor
        Map<String, Object> creditor = new HashMap<>();
        creditor.put("name", input.getCreditor().getName());
        creditor.put("account", account);
        creditor.put("address", address);

        requestBody.put("creditor", creditor);
        requestBody.put("executionDate", input.getExecutionDate());
        requestBody.put("uri", input.getUri());
        requestBody.put("description", input.getDescription());
        requestBody.put("amount", input.getAmount());
        requestBody.put("currency", input.getCurrency());
        requestBody.put("isUrgent", input.isUrgent());
        requestBody.put("isInstant", input.isInstant());
        requestBody.put("feeType", input.getFeeType());
        requestBody.put("feeAccountId", input.getFeeAccountId());

        // Creazione dell'oggetto naturalPersonBeneficiary
        Map<String, Object> naturalPersonBeneficiary = new HashMap<>();
        naturalPersonBeneficiary.put("fiscalCode1", input.getTaxRelief().getNaturalPersonBeneficiary().getFiscalCode1());
        naturalPersonBeneficiary.put("fiscalCode2", input.getTaxRelief().getNaturalPersonBeneficiary().getFiscalCode2());
        naturalPersonBeneficiary.put("fiscalCode3", input.getTaxRelief().getNaturalPersonBeneficiary().getFiscalCode3());
        naturalPersonBeneficiary.put("fiscalCode4", input.getTaxRelief().getNaturalPersonBeneficiary().getFiscalCode4());
        naturalPersonBeneficiary.put("fiscalCode5", input.getTaxRelief().getNaturalPersonBeneficiary().getFiscalCode5());

        // Creazione dell'oggetto legalPersonBeneficiary
        Map<String, Object> legalPersonBeneficiary = new HashMap<>();
        legalPersonBeneficiary.put("fiscalCode", input.getTaxRelief().getLegalPersonBeneficiary().getFiscalCode());
        legalPersonBeneficiary.put("legalRepresentativeFiscalCode", input.getTaxRelief().getLegalPersonBeneficiary().getLegalRepresentativeFiscalCode());

        // Creazione dell'oggetto taxRelief
        Map<String, Object> taxRelief = new HashMap<>();
        taxRelief.put("taxReliefId", input.getTaxRelief().getTaxReliefId());
        taxRelief.put("isCondoUpgrade", input.getTaxRelief().isCondoUpgrade());
        taxRelief.put("creditorFiscalCode", input.getTaxRelief().getCreditorFiscalCode());
        taxRelief.put("beneficiaryType", input.getTaxRelief().getBeneficiaryType());
        taxRelief.put("naturalPersonBeneficiary", naturalPersonBeneficiary);
        taxRelief.put("legalPersonBeneficiary", legalPersonBeneficiary);

        requestBody.put("taxRelief", taxRelief);

        // Creazione dell'oggetto HttpEntity con il corpo della richiesta e gli header
        log.debug("Creazione dell'oggetto HttpEntity con il corpo della richiesta e gli header");
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // Effettua la chiamata REST
            log.info("Invio della richiesta POST");
            ResponseEntity<String> response = restTemplate.exchange(
                    cashProperties.getBaseUrl() + accountId + "/payments/money-transfers",
                    HttpMethod.POST,
                    requestEntity,
                    String.class);

            // Processa la risposta da Fabrick
            log.info("Lettura della risposta");
            return response.getBody();

        } catch (HttpClientErrorException e) {
            // Errori client come 400 Bad Request, 404 Not Found, ecc.
            return "Errore client: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();

        } catch (HttpServerErrorException e) {
            // Errori server come 500 Internal Server Error, 503 Service Unavailable, ecc.
            return "Errore server: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();

        } catch (ResourceAccessException e) {
            // Problemi di connettività, come timeout
            return "Problemi di connettività: " + e.getMessage();

        } catch (Exception e) {
            // Gestisci altre eccezioni generiche
            // Potrebbe essere utile loggare l'errore o eseguire azioni correttive
            return "Errore generico: " + e.getMessage();
        }

    }

    public List<TransactionAccountDTO> getTransactions(String accountId, String fromAccountingDate, String toAccountingDate) throws JsonProcessingException {

        // Chiamata all'API esterna
        List<TransactionAccountDTO> transactions = fetchTransactionsFromApi(accountId, fromAccountingDate, toAccountingDate);

        if (CollectionUtils.isEmpty(transactions)) {
            throw new NoTransactionsFoundException("No transactions found for the given date range.");
        }

        // Scrittura nel database
        transactionRepository.saveAll(TransactionAccountMapper.INSTANCE.transactionAccountDTOsToTransactionAccounts(transactions));

        // Ritorno la lista delle transazioni
        return transactions;
    }

    private List<TransactionAccountDTO> fetchTransactionsFromApi(String accountId, String fromAccountingDate, String toAccountingDate) throws JsonProcessingException {
        List<TransactionAccountDTO> transactions = new ArrayList<>();
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set(AUTH_SCHEMA, cashProperties.getSchema());
            headers.set(API_KEY, cashProperties.getKey());

            HttpEntity<String> entity = new HttpEntity<>("body", headers);

            String url = cashProperties.getBaseUrl() + accountId + "/transactions?fromAccountingDate=" + fromAccountingDate + "&toAccountingDate=" + toAccountingDate;

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCodeValue() == 200 && response.getBody() != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode listNode = root.path("payload").path("list");

                if (listNode.isArray()) {
                    for (JsonNode node : listNode) {
                        TransactionAccountDTO transaction = new TransactionAccountDTO();
                        transaction.setTransactionId(node.path("transactionId").asText());
                        transaction.setOperationId(node.path("operationId").asText());
                        transaction.setAccountingDate(new SimpleDateFormat(YYYY_MM_DD).parse(node.path("accountingDate").asText()));
                        transaction.setValueDate(new SimpleDateFormat(YYYY_MM_DD).parse(node.path("valueDate").asText()));
                        transaction.setTransactionType(node.path("type").path("value").asText());
                        transaction.setAmount(node.path("amount").asDouble());
                        transaction.setCurrency(node.path("currency").asText());
                        transaction.setDescription(node.path("description").asText());
                        transactions.add(transaction);
                    }
                } else {
                    throw new InvalidResponseException("Response JSON is not as expected");
                }
            } else {
                throw new ApiException("API returned with status: " + response.getStatusCodeValue());
            }
        } catch (RestClientException e) {
            throw new ApiException("Failed to call API: " + getErrorMessage(e));
        } catch (JsonProcessingException e) {
            throw new InvalidResponseException("Failed to parse JSON: " + getErrorMessage(e));
        } catch (Exception e) {
            throw new UnexpectedApiException("An unexpected error occurred: " + e.getMessage(), e);
        }

        return transactions;
    }

    private String getErrorMessage(Exception e) throws JsonProcessingException {
        int startIndex = ((HttpClientErrorException) e).getResponseBodyAsString().indexOf("{");
        int endIndex = ((HttpClientErrorException) e).getResponseBodyAsString().lastIndexOf("}");
        String content = ((HttpClientErrorException) e).getResponseBodyAsString().substring(startIndex, endIndex + 1);
        JsonNode jsonNode = new ObjectMapper().readTree(content);

        return jsonNode.get("errors").findValuesAsText("description").get(0);
    }
}
