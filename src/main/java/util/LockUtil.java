package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public interface LockUtil {
    Logger LOG = LoggerFactory.getLogger(LockUtil.class);

    static boolean tryLockAll(List<Lock> locks) throws InterruptedException {

        for (int i = 0; i < locks.size(); ++i) {
            try {
                if (!locks.get(i).tryLock(1 + i, TimeUnit.SECONDS)) {
                    // Fail in obtain lock, so release successed locks
                    // so other threads may use it
                    LOG.trace("Fail to obtain lock {}/{}", (i + 1), locks.size());
                    unlockInReverseOrder(locks.subList(0, i));
                    return false;

                }
            } catch (InterruptedException ex) {
                // current thread was interrupted
                unlockInReverseOrder(locks.subList(0, i));
                throw ex;
            }
        }
        return true;
    }

    static void unlockInReverseOrder(List<Lock> locks) {
        for (int i = locks.size() - 1; i >= 0; --i) {
            locks.get(i).unlock();
        }
    }

    static void lockOrThrow(Lock lock, int timeoutSec) {

        try {
            if (!lock.tryLock(timeoutSec, TimeUnit.SECONDS)) {
                throw new RuntimeException("Fail to obtain lock in "+ timeoutSec + " sec");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CancellationException("Operation canceled");
        }

    }
}
