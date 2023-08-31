package it.evoluta.fabrickaccount.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BalancePayload implements Serializable {

    private String date;

    private double balance;

    private double availableBalance;

    private String currency;
}
