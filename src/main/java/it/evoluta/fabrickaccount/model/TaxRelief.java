package it.evoluta.fabrickaccount.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
public class TaxRelief implements Serializable {

    public String taxReliefId;

    @NotNull
    public boolean isCondoUpgrade;

    @NotNull
    public String creditorFiscalCode;

    @NotNull
    public String beneficiaryType;

    public NaturalPersonBeneficiary naturalPersonBeneficiary;

    public LegalPersonBeneficiary legalPersonBeneficiary;
}
