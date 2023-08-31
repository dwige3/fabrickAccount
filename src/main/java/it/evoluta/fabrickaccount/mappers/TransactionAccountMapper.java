package it.evoluta.fabrickaccount.mappers;

import it.evoluta.fabrickaccount.model.TransactionAccount;
import it.evoluta.fabrickaccount.model.TransactionAccountDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionAccountMapper {

    TransactionAccountMapper INSTANCE = Mappers.getMapper(TransactionAccountMapper.class);
    @Mapping(source = "accountingDate", target = "accountingDate", dateFormat = "yyyy-MM-dd")
    TransactionAccount transactionAccountDTOToTransactionAccount(TransactionAccountDTO transactionAccountDTO);

    List<TransactionAccount> transactionAccountDTOsToTransactionAccounts(List<TransactionAccountDTO> transactions);


}
