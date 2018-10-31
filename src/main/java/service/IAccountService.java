package service;

import model.Account;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Created by kirio on 31.10.2018.
 */
public interface IAccountService {
    Account create(BigDecimal balance);
    Account get(long id);
    Collection<Account> getAll();
}
