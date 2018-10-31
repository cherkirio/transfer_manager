package endpoint;

import endpoint.util.JsonUtil;
import endpoint.util.RequestUtil;
import model.Account;
import model.BadParamException;
import model.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.IAccountService;
import service.TransferService;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

import static spark.Spark.*;

/**
 * Created by kirio on 29.10.2018.
 */
public class Server {


    private final static Logger LOG = LoggerFactory.getLogger(Server.class);


    private TransferService transferService;

    private IAccountService accountService;

    @Inject
    public void setAccountService(IAccountService accountService) {
        this.accountService = accountService;
    }

    @Inject
    public void setTransferService(TransferService transferService) {
        this.transferService = transferService;
    }

    public void init() {
        port(8080);
        initErrorHandlers();
        initAccountEndpoint();

    }

    private void initErrorHandlers() {
        ResponseTransformer json = JsonUtil.json();
        internalServerError((req, res) -> json.render(new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal error")));
        notFound((req, res) -> json.render(new ErrorMessage(HttpServletResponse.SC_NOT_FOUND, "not found error")));
        exception(BadParamException.class, new ExceptionHandler<BadParamException>() {
            @Override
            public void handle(BadParamException e, Request request, Response response) {
                response.status(HttpServletResponse.SC_BAD_REQUEST);
                try {
                    response.body(json.render(new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST, e.getMessage())));
                } catch (Exception ignore) {
                    LOG.error("cant serialise exception {}", e, ignore);
                }
            }
        });
        exception(EntityNotFoundException.class, new ExceptionHandler<EntityNotFoundException>() {
            @Override
            public void handle(EntityNotFoundException e, Request request, Response response) {
                response.status(HttpServletResponse.SC_NOT_FOUND);
                try {
                    response.body(json.render(new ErrorMessage(HttpServletResponse.SC_NOT_FOUND, e.getMessage())));
                } catch (Exception ignore) {
                    LOG.error("cant serialise exception {}", e, ignore);
                }
            }
        });
    }

    private void initAccountEndpoint() {

        final String path = "account";

        before(String.format("/%s/*", path),
                (request, response) -> response.type("application/json"),

                (request, response) -> LOG.debug("{}{}", request.uri(), request.queryString() == null ? "" : ("?" + request.queryString()))
        );

        path(String.format("/%s", path), () -> {

            get("/new", this::newAccount, JsonUtil.json()); //  query params: balance

            get("/all", (req,res)->accountService.getAll(), JsonUtil.json()); //  query params: balance


            get("/:id", this::accountInfo, JsonUtil.json());


            get("/:src_id/transfer_to/:dst_id", this::transfer, JsonUtil.json());  //  query params: amount

        });
    }


    private Object newAccount(Request request, Response responce) throws BadParamException {

        BigDecimal balance = RequestUtil.bigDecimalQueryParam(request, "balance");
        return accountService.create(balance);

    }

    private Object accountInfo(Request request, Response response) {

        Long id = RequestUtil.longPathParam(request, ":id");
        Account account = accountService.get(id);
        if (account == null) {
            throw new EntityNotFoundException("Account " + id + " not found");
        } else {
            return account;
        }

    }

    private Object transfer(Request request, Response responce) {


        BigDecimal amount;
        try {
            amount = RequestUtil.bigDecimalQueryParam(request, "amount");
        } catch (Exception ex) {
            responce.status(HttpServletResponse.SC_BAD_REQUEST);
            return new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        }

        long srcId = Long.parseLong(request.params(":src_id"));
        long dstId = Long.parseLong(request.params(":dst_id"));

        return "GOOD" + srcId + " - " + dstId + " " + amount;
    }


}
