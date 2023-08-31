package it.evoluta.fabrickaccount.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.evoluta.fabrickaccount.model.BalancePayload;
import it.evoluta.fabrickaccount.model.BonificoInput;
import it.evoluta.fabrickaccount.model.TransactionAccountDTO;
import it.evoluta.fabrickaccount.service.CashService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/accounts")
@Api(value = "/api/accounts", produces = MediaType.APPLICATION_JSON_VALUE, tags = "Bank Accounts")
public class CashController {

    private final CashService cashService;

    public CashController(CashService cashService) {
        this.cashService = cashService;
    }

    @ApiOperation(value = "Recupera il saldo di uno specifico Account.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 400, message = "Dati mancanti e/o errati"),
            @ApiResponse(code = 401, message = "Non autorizzato"),
            @ApiResponse(code = 403, message = "Non autorizzato"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping(path = "/{accountId}/balance", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BalancePayload> getBalance(@PathVariable(value = "accountId") @ApiParam(name = "accountId", required = true, value = "Identificativo dell'account") Long accountId) throws JsonProcessingException {

        log.info("Inizio richiesta: /api/accounts/{accountId}/balance/ with accountId: {}", accountId);
        BalancePayload balance = cashService.accountBalance(accountId);
        log.info("Success: /api/accounts/{accountId}/balance accountId: {}", accountId);
        return new ResponseEntity<>(balance, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Create Money Transfer",
            notes = "Creates a new money transfer",
            response = String.class,
            produces = "application/json")
    @ApiResponses(value =
            {@ApiResponse(code = 200, message = "created money transfer"),
                    @ApiResponse(code = 500, message = "Errore interno del server"),
                    @ApiResponse(code = 404, message = "NOT FOUND")
            })
    @PostMapping(value = "/{accountId}/payments/money-transfers")
    public ResponseEntity<String> effettuaBonifico(@Valid @RequestBody BonificoInput input,
                                                   @ApiParam("The ID of the account") @PathVariable @NotNull String accountId) {

        log.info("Calling 'effettuaBonifico' accountId: {}", accountId);

        String response = cashService.effettuaBonifico(input, accountId);

        log.info("Finish request 'effettuaBonifico' accountId: {}", accountId);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(
            value = "Retrieves the transactions of a specific cash account.",
            notes = " Restituisce una collezione di transactions in formato JSON",
            response = TransactionAccountDTO.class,
            produces = "application/json")
    @ApiResponses(value =
            {@ApiResponse(code = 200, message = "lista delle transactions "),
                    @ApiResponse(code = 401, message = "Non sei autenticato"),
                    @ApiResponse(code = 500, message = "Internal Server Error")
            })
    @GetMapping(path = "/{accountId}/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TransactionAccountDTO>> getTransactions(@ApiParam("The ID of the account") @PathVariable(value = "accountId") String accountId,
                                                                       @RequestParam(value = "fromAccountingDate") String fromAccountingDate,
                                                                       @RequestParam(value = "toAccountingDate") String toAccountingDate) throws JsonProcessingException {

        log.info("Inizio richiesta: /api/accounts/{accountId}/transactions accountId: {}, fromAccountingDate: {}, toAccountingDate: {}", accountId, fromAccountingDate, toAccountingDate);

        List<TransactionAccountDTO> transactionAccounts = cashService.getTransactions(accountId, fromAccountingDate, toAccountingDate);

        log.info("Success: /api/accounts/{accountId}/transactions accountId: {}, fromAccountingDate: {}, toAccountingDate: {}", accountId, fromAccountingDate, toAccountingDate);
        return new ResponseEntity<>(transactionAccounts, HttpStatus.OK);
    }

}
