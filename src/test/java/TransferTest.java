import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import config.ConfigModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import service.IAccountService;

import java.util.concurrent.CompletableFuture;

public class TransferTest {

    @Inject
    private IAccountService accountService;


    @Before
    public void init() {
        Injector injector = Guice.createInjector(new ConfigModule());
        injector.injectMembers(this);
    }


    @Test
    public void testTransfer() {
        final long firstId = accountService.create(1_000_000).getId();
        final long secondId = accountService.create(0).getId();


        Assert.assertEquals(1_000_000, accountService.get(firstId).getBalance().intValue());
        Assert.assertEquals(0, accountService.get(secondId).getBalance().intValue());

        Runnable fromFirstToSecond = () -> {
            for (int i = 0; i < 100; ++i) {
                accountService.transfer(firstId, secondId, 100);
            }
        };
        Runnable fromSecondToFirst = () -> {
            for (int i = 0; i < 1000; ++i) {
                accountService.transfer(secondId, firstId, 10);
            }
        };
        CompletableFuture.allOf(

                CompletableFuture.runAsync(fromSecondToFirst),

                CompletableFuture.runAsync(fromFirstToSecond),
                CompletableFuture.runAsync(fromFirstToSecond),
                CompletableFuture.runAsync(fromFirstToSecond),
                CompletableFuture.runAsync(fromFirstToSecond),
                CompletableFuture.runAsync(fromFirstToSecond),
                CompletableFuture.runAsync(fromFirstToSecond),

                CompletableFuture.runAsync(fromSecondToFirst)
        ).join();

        // 6 threads -> fromFirstToSecond (100 times send 100)
        // 2 threads -> fromFirstToSecond (1000 times send 10)
        Assert.assertEquals(1_000_000 - (6 * 100 * 100) + (2 * 1000 * 10), accountService.get(firstId).getBalance().intValue());
        Assert.assertEquals(6 * 100 * 100 - (2 * 1000 * 10), accountService.get(secondId).getBalance().intValue());
    }


}
