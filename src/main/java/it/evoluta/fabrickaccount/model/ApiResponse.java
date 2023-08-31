package it.evoluta.fabrickaccount.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiResponse {

    private String status;

    private List<String> error;

    private BalancePayload payload;

}
