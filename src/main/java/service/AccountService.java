package service;

import endpoint.dao.AccountDao;
import model.Account;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.Collection;

/**
 * Created by kirio on 31.10.2018.
 */
@Singleton
public class AccountService implements IAccountService {

    private AccountDao accountDao;

    @Inject
    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public Account create(BigDecimal balance) {
        return accountDao.create(balance);
    }

    @Override
    public Account get(long id) {
        return accountDao.get(id);
    }

    @Override
    public Collection<Account> getAll() {
        return accountDao.getAll();
    }
}
