package server;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import model.Account;
import model.ErrorMessage;
import model.TransferTransaction;
import model.exception.BadParamException;
import model.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AccountService;
import spark.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

import static spark.Spark.*;

/**
 * Created by kirio on 29.10.2018.
 */
public class Server {


    private final static Logger LOG = LoggerFactory.getLogger(Server.class);

    @Inject
    private AccountService accountService;

    @Inject
    @Named("server.port")
    private int port;


    public void init() {
        port(port);
        initErrorHandlers();
        initAccountEndpoint();

    }

    public void stop() {
        Spark.stop();
    }


    private void initAccountEndpoint() {


        final String pathAccount = "account";

        before("/" + pathAccount + "/*",
                (request, response) -> response.type("application/json"),

                (request, response) -> LOG.debug("{}{}", request.uri(), request.queryString() == null ? "" : ("?" + request.queryString()))
        );

        path("/" + pathAccount, () -> {

            put("/new", this::newAccount, jsonTransformer); //  query params: balance


            get("/:id", this::accountInfo, jsonTransformer);


            post("/:src_id/transfer/:dst_id", this::transfer, jsonTransformer);  //  query params: amount

        });
    }


    private Account newAccount(Request request, Response response) throws BadParamException {

        BigDecimal balance = RequestUtil.bigDecimalQueryParam(request, "balance");
        return accountService.create(balance);

    }

    private Account accountInfo(Request request, Response response) {

        Long id = RequestUtil.longPathParam(request, ":id");
        Account account = accountService.get(id);
        if (account == null) {
            throw new EntityNotFoundException("Account " + id + " not found");
        } else {
            return account;
        }

    }

    private TransferTransaction transfer(Request request, Response response) throws BadParamException {

        BigDecimal amount = RequestUtil.bigDecimalQueryParam(request, "amount");
        long srcId = RequestUtil.longPathParam(request, ":src_id");
        long dstId = RequestUtil.longPathParam(request, ":dst_id");

        return accountService.transfer(srcId, dstId, amount);
    }


    private void initErrorHandlers() {

        internalServerError((req, res) -> jsonTransformer.render(new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal error")));
        notFound((req, res) -> jsonTransformer.render(new ErrorMessage(HttpServletResponse.SC_NOT_FOUND, "not found error")));
        exception(Exception.class, new ExceptionHandler<Exception>() {
            @Override
            public void handle(Exception e, Request request, Response response) {
                if (e instanceof BadParamException) {
                    response.status(HttpServletResponse.SC_BAD_REQUEST);
                } else if (e instanceof EntityNotFoundException) {
                    response.status(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    LOG.error("Unexpected error:", e);
                    e = new Exception("Unexpected error"); // do not send real exception outside
                }
                try {
                    response.body(jsonTransformer.render(new ErrorMessage(response.status(), e.getMessage())));
                } catch (Exception ignore) {
                    LOG.error("cant serialise exception:", ignore);
                }
            }
        });
        exception(EntityNotFoundException.class, new ExceptionHandler<EntityNotFoundException>() {
            @Override
            public void handle(EntityNotFoundException e, Request request, Response response) {
                response.status(HttpServletResponse.SC_NOT_FOUND);
                try {
                    response.body(jsonTransformer.render(new ErrorMessage(HttpServletResponse.SC_NOT_FOUND, e.getMessage())));
                } catch (Exception ignore) {
                    LOG.error("cant serialise exception:", ignore);
                }
            }
        });
    }


    private final static ResponseTransformer jsonTransformer = new ResponseTransformer() {
        final Gson gson = new Gson();

        @Override
        public String render(Object o) throws Exception {
            return gson.toJson(o);
        }
    };

}
