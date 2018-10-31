package endpoint.dao;

import model.Account;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Collection;

/**
 * Created by kirio on 29.10.2018.
 */
@Singleton
public class AccountDao {

    private EntityManager entityManager;

    @Inject
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public Account create(@Nonnull BigDecimal balance) {

        Account account = new Account();
        account.setBalance(balance);
        return save(account);

    }

    public Account get(long id) {
        return entityManager.find(Account.class, id);
    }

    @SuppressWarnings("unchecked")
    public Collection<Account> getAll() {
        return (Collection<Account>) entityManager.createQuery("Select t from Account  t").getResultList();
    }

    public Account save(@Nonnull Account account) {
        getEntityManager().getTransaction().begin();

        entityManager.persist(account);
        getEntityManager().getTransaction().commit();

        return account;
    }

    public Account[] saveAll(@Nonnull Account... accounts) {
        getEntityManager().getTransaction().begin();
        for (Account account : accounts) {
            save(account);
        }
        getEntityManager().getTransaction().commit();
        return accounts;

    }

}
