package endpoint;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import model.Account;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import service.AccountService;
import service.IAccountService;
import service.ITransferService;
import service.TransferService;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.util.Properties;

/**
 * Created by kirio on 31.10.2018.
 */
public class Config implements Module {
    private static final ThreadLocal<EntityManager> ENTITY_MANAGER_CACHE
            = new ThreadLocal<>();

    @Override
    public void configure(Binder binder) {
        binder.bind(ITransferService.class).to(TransferService.class);
        binder.bind(IAccountService.class).to(AccountService.class);
    }

    @Provides
    @Singleton
    SessionFactory sessionFactory() {

        Properties prop = new Properties();
        prop.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        prop.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        prop.setProperty("hibernate.connection.url", "jdbc:h2:./test");
        prop.setProperty("hibernate.connection.username", "sa");
        prop.setProperty("hibernate.connection.password", "sa");
        prop.setProperty("hibernate.show_sql", "true");
        prop.setProperty("hibernate.hbm2ddl.auto", "create");
        prop.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");
        Configuration configuration = new Configuration();

        return configuration
                .addAnnotatedClass(Account.class)
                .addProperties(prop)
                .buildSessionFactory();

    }

    @Provides
    EntityManager entityManager(SessionFactory sessionFactory) {
        EntityManager entityManager = ENTITY_MANAGER_CACHE.get();
        if (entityManager == null) {
            ENTITY_MANAGER_CACHE.set(entityManager = sessionFactory.createEntityManager());
        }
        return entityManager;
    }
}
