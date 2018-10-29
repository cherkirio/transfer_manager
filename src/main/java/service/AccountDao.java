package service;

import model.Account;

import java.math.BigDecimal;

/**
 * Created by kirio on 29.10.2018.
 */
public class AccountDao {
    public Account newAccount(BigDecimal balance) {
        return new Account(1, balance);
    }

    public Account byId(long id) {
        return new Account(1, null);
    }
}
