package it.evoluta.fabrickaccount.repository;

import it.evoluta.fabrickaccount.model.TransactionAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionAccount, String> {

}
