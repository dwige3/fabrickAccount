package it.evoluta.fabrickaccount.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transaction_account")
public class TransactionAccount implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "transactionId")
    private String transactionId;

    @Column(name = "operationId", nullable = true)
    private String operationId;

    @Column(name = "accountingDate")
    private Date accountingDate;

    @Column(name = "valueDate")
    private Date valueDate;

    @Column(name = "transactionType")
    private String transactionType;

    @Column(name = "amount", nullable = true)
    private Double amount;

    @Column(name = "currency", nullable = true)
    private String currency;

    @Column(name = "description")
    private String description;

}
