package it.evoluta.fabrickaccount.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
public class NaturalPersonBeneficiary implements Serializable {

    @NotNull
    private String fiscalCode1;

    private String fiscalCode2;

    private String fiscalCode3;

    private String fiscalCode4;

    private String fiscalCode5;

}
