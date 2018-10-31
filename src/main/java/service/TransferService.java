package service;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Striped;
import com.google.inject.Inject;
import endpoint.dao.AccountDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;

/**
 * Created by kirio on 31.10.2018.
 */
@Singleton
public class TransferService implements ITransferService {

    private final static Logger LOG = LoggerFactory.getLogger(TransferService.class);

    private final Striped<Lock> stripedLock;

    private AccountDao accountDao;

    @Inject
    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public TransferService() {
        this.stripedLock = Striped.lazyWeakLock(Runtime.getRuntime().availableProcessors() * 4);
    }

    @Override
    public void transfer(long from_id, long to_id, BigDecimal amount) {

        LOG.debug("transfer from [{}] to [{}]: {}", from_id, to_id, amount);
        if (from_id == to_id) {
            throw new IllegalArgumentException("Self transfer detected");
        }
        if (amount == null || amount.compareTo(BigDecimal.ONE) <= 0) {
            throw new IllegalArgumentException("Invalid transfer amount");
        }

        Iterable<Lock> locks = stripedLock.bulkGet(ImmutableList.of(from_id, to_id));



    }
}
