package it.evoluta.fabrickaccount.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class TransactionAccountDTO  implements Serializable {

    private String transactionId;

    private String operationId;

    private Date accountingDate;

    private Date valueDate;

    private String transactionType;

    private Double amount;

    private String currency;

    private String description;
}
