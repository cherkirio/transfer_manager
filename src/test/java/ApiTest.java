import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import config.ConfigModule;
import model.Account;
import model.ErrorMessage;
import model.TransferStatus;
import model.TransferTransaction;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import server.Server;

import java.math.BigDecimal;

public class ApiTest {

    private static Gson gson;
    private static Server server;

    @BeforeClass
    public static void init() {
        Injector injector = Guice.createInjector(new ConfigModule());
        server = new Server();
        injector.injectMembers(server);
        server.init();
        gson = new Gson();
    }

    @AfterClass
    public static void shutdownServer() {
        server.stop();
    }

    @Test
    public void testEndpoints() throws Exception {

        HttpResponse<String> response = Unirest.put("http://localhost:8080/account/new")
                .queryString("balance", "10000")
                .asString();
        Account account = gson.fromJson(response.getBody(), Account.class);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(1, account.getId());
        Assert.assertEquals(BigDecimal.valueOf(10000), account.getBalance());

        response = Unirest.put("http://localhost:8080/account/new")
                .queryString("balance", "500")
                .asString();

        account = gson.fromJson(response.getBody(), Account.class);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(2, account.getId());
        Assert.assertEquals(BigDecimal.valueOf(500), account.getBalance());


        response = Unirest.get("http://localhost:8080/account/1")
                .asString();
        account = gson.fromJson(response.getBody(), Account.class);
        Assert.assertEquals(1, account.getId());
        Assert.assertEquals(BigDecimal.valueOf(10000), account.getBalance());


        response = Unirest.post("http://localhost:8080/account/2/transfer/1")
                .field("amount", "250")
                .asString();

        TransferTransaction transaction = gson.fromJson(response.getBody(), TransferTransaction.class);
        Assert.assertEquals(2, transaction.getFromAccount());
        Assert.assertEquals(1, transaction.getToAccount());
        Assert.assertEquals(TransferStatus.Success, transaction.getStatus());

        response = Unirest.get("http://localhost:8080/account/1")
                .asString();
        account = gson.fromJson(response.getBody(), Account.class);
        Assert.assertEquals(1, account.getId());
        Assert.assertEquals(BigDecimal.valueOf(10000 + 250), account.getBalance());

        response = Unirest.get("http://localhost:8080/account/2")
                .asString();
        account = gson.fromJson(response.getBody(), Account.class);
        Assert.assertEquals(2, account.getId());
        Assert.assertEquals(BigDecimal.valueOf(500 - 250), account.getBalance());

        response = Unirest.post("http://localhost:8080/account/1/transfer/99")
                .field("amount", "10")
                .asString();

        transaction = gson.fromJson(response.getBody(), TransferTransaction.class);
        Assert.assertEquals(1, transaction.getFromAccount());
        Assert.assertEquals(99, transaction.getToAccount());
        Assert.assertEquals(TransferStatus.Error, transaction.getStatus());


        response = Unirest.post("http://localhost:8080/account/1/transfer/2")
                .queryString("amount", "-10")
                .asString();

        transaction = gson.fromJson(response.getBody(), TransferTransaction.class);
        Assert.assertEquals(TransferStatus.Invalid, transaction.getStatus());

        response = Unirest.post("http://localhost:8080/account/1/transfer/1")
                .queryString("amount", "100")
                .asString();

        transaction = gson.fromJson(response.getBody(), TransferTransaction.class);
        Assert.assertEquals(TransferStatus.Invalid, transaction.getStatus());

    }

    @Test
    public void testInvalidRequestParams() throws Exception {
        HttpResponse<String> response = Unirest.get("http://localhost:8080/account/new")
                .asString();
        Assert.assertEquals(400, response.getStatus());
        ErrorMessage errorMessage = gson.fromJson(response.getBody(), ErrorMessage.class);
        Assert.assertEquals(400, errorMessage.getCode());


        Unirest.get("http://localhost:8080/account/new")
                .queryString("amount", "not digit")
                .asString();
        Assert.assertEquals(400, response.getStatus());
        errorMessage = gson.fromJson(response.getBody(), ErrorMessage.class);
        Assert.assertEquals(400, errorMessage.getCode());


        response = Unirest.post("http://localhost:8080/account/1/transfer/1")
                .asString();
        Assert.assertEquals(400, response.getStatus());
        errorMessage = gson.fromJson(response.getBody(), ErrorMessage.class);
        Assert.assertEquals(400, errorMessage.getCode());

        response = Unirest.post("http://localhost:8080/account/1/transfer/1")
                .field("amount", "not digit")
                .asString();
        Assert.assertEquals(400, response.getStatus());
        errorMessage = gson.fromJson(response.getBody(), ErrorMessage.class);
        Assert.assertEquals(400, errorMessage.getCode());
    }

    @Test
    public void testInvalidTransactions() throws Exception {
        HttpResponse<String> response = Unirest.post("http://localhost:8080/account/1/transfer/2")
                .field("amount", "-10")
                .asString();

        TransferTransaction transaction = gson.fromJson(response.getBody(), TransferTransaction.class);
        Assert.assertEquals(TransferStatus.Invalid, transaction.getStatus());

        response = Unirest.post("http://localhost:8080/account/1/transfer/1")
                .field("amount", "100")
                .asString();

        transaction = gson.fromJson(response.getBody(), TransferTransaction.class);
        Assert.assertEquals(TransferStatus.Invalid, transaction.getStatus());
    }


}
