package it.evoluta.fabrickaccount.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
public class Account implements Serializable {

    @NotNull
    public String accountCode;

    public String bicCode;
}
