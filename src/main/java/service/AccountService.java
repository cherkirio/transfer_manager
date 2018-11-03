package service;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Striped;
import model.Account;
import model.TransferStatus;
import model.TransferTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.LockUtil;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

/**
 * Created by kirio on 31.10.2018.
 */
@Singleton
public class AccountService implements IAccountService {
    private final static Logger LOG = LoggerFactory.getLogger(AccountService.class);

    private final AtomicLong accountIdGenerator;
    private final AtomicLong transactionIdGenerator;
    private final Map<Long, Account> dataStore;
    private final Striped<ReadWriteLock> stripedLock;

    private int transferTimeout = 30_000;
    private int readTimeout = 30_000;


    public AccountService() {
        stripedLock = Striped.lazyWeakReadWriteLock(Runtime.getRuntime().availableProcessors() * 4);
        accountIdGenerator = new AtomicLong();
        transactionIdGenerator = new AtomicLong();
        dataStore = new ConcurrentHashMap<>();

    }

    @Override
    public Account create(@Nonnull BigDecimal balance) {
        Preconditions.checkNotNull(balance);
        Account account = new Account(accountIdGenerator.incrementAndGet(), balance);
        dataStore.put(account.getId(), account);
        return account;
    }


    @Override
    public Account get(long id) {

        // for a sake of simplicity: this method will wait until transfer operation finished and write lock released
        // there is another way for implementing this method for returning snapshot account value without waiting
        // (but this require change transfer and this methods)
        Lock readLock = stripedLock.get(id).readLock();
        LockUtil.lockOrThrow(readLock, readTimeout);

        try {
            Account account = dataStore.get(id);
            // Return copy of account
            return account == null ? null : new Account(account);

        } finally {
            readLock.unlock();
        }
    }

    @Override
    public TransferTransaction transfer(long from_id, long to_id, BigDecimal amount) {
        TransferTransaction trans = new TransferTransaction(transactionIdGenerator.incrementAndGet(), from_id, to_id, amount);
        LOG.debug("Transfer[{}]: from [{}] to [{}]: {}", trans.getId(), from_id, to_id, amount);

        if (from_id == to_id) {
            LOG.error("Transfer[{}]: self transfer detected: id [{}], amount [{}]", trans.getId(), from_id, amount);
            return trans.status(TransferStatus.Invalid);

        }
        if (amount == null || amount.compareTo(BigDecimal.ONE) <= 0) {
            LOG.error("Transfer[{}]: fail to transfer from [{}] to [{}]: invalid transfer amount {}", trans.getId(), from_id, to_id, amount);
            return trans.status(TransferStatus.Invalid);

        }
        List<Lock> writeLocks = ImmutableList.copyOf(stripedLock.bulkGet(ImmutableList.of(from_id, to_id)))
                .stream().map(ReadWriteLock::writeLock).collect(Collectors.toList());

        Stopwatch stopwatch = Stopwatch.createStarted();
        while (stopwatch.elapsed(TimeUnit.MILLISECONDS) <= transferTimeout) {
            try {
                if (!LockUtil.tryLockAll(writeLocks)) {
                    // fail to obtain all locks -> go to next try
                    continue;
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOG.error("Transfer[{}]: transfer aborted due current thread was interrupted: from [{}] to [{}] amount [{}]",
                        trans.getId(), from_id, to_id, amount);
                return trans.status(TransferStatus.Canceled);

            }

            try {


                transferInternal(from_id, to_id, amount);
                return trans.status(TransferStatus.Success);


            } catch (Exception ex) {
                LOG.error("Transfer[{}]: fail to transfer from [{}] to [{}] amount [{}]: {}",
                        trans.getId(), from_id, to_id, amount, ex.getMessage(), ex);
                return trans.status(TransferStatus.Error);

            } finally {
                LockUtil.unlockInReverseOrder(writeLocks);
            }


        }
        LOG.error("Transfer[{}]: timeout, fail to transfer from [{}] to [{}] amount [{}]: fail to obtain lock in {} ms",
                trans.getId(), from_id, to_id, amount, transferTimeout);
        return trans.status(TransferStatus.Timeout);
    }


    private void transferInternal(long from_id, long to_id, BigDecimal amount) {

        Account accountFrom = dataStore.get(from_id);
        if (accountFrom == null) {
            throw new RuntimeException("Account [" + from_id + "] is not found");
        }
        Account accountTo = dataStore.get(to_id);
        if (accountTo == null) {
            throw new RuntimeException("Account [" + to_id + "] is not found");
        }
        accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
        accountTo.setBalance(accountTo.getBalance().add(amount));

        dataStore.put(accountTo.getId(), accountTo);
        dataStore.put(accountFrom.getId(), accountFrom);


    }

}
