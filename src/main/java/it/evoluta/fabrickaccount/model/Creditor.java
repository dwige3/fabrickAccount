package it.evoluta.fabrickaccount.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
@Getter
@Setter
public class Creditor implements Serializable {

    @NotNull
    public String name;

    @NotNull
    public Account account;

    public Address address;
}
