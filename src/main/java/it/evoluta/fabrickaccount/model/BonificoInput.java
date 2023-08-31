package it.evoluta.fabrickaccount.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
public class BonificoInput implements Serializable {

    @JsonProperty("creditor")
    @NotNull
    private Creditor creditor;

    @JsonProperty("executionDate")
    private String executionDate;

    @JsonProperty("uri")
    private String uri;

    @JsonProperty("description")
    @NotNull
    private String description;

    @JsonProperty("amount")
    @NotNull
    private double amount;

    @JsonProperty("currency")
    @NotNull
    private String currency;

    @JsonProperty("isUrgent")
    private boolean isUrgent;

    @JsonProperty("isInstant")
    private boolean isInstant;

    @JsonProperty("feeType")
    private String feeType;

    @JsonProperty("feeAccountId")
    private String feeAccountId;

    @JsonProperty("taxRelief")
    private TaxRelief taxRelief;
}
