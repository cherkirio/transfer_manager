package service;

import model.Account;
import model.TransferTransaction;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

/**
 * Created by kirio on 31.10.2018.
 */
public interface IAccountService {

    Account create(@Nonnull BigDecimal balance);
    default Account create(double balance) {
        return this.create(BigDecimal.valueOf(balance));
    }

    Account get(long id);

    TransferTransaction transfer(long from_id, long to_id, BigDecimal amount);

    default TransferTransaction transfer(long from_id, long to_id, double amount) {
        return transfer(from_id, to_id, BigDecimal.valueOf(amount));
    }

}
