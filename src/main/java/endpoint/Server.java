package endpoint;

import endpoint.util.JsonUtil;
import model.Account;
import model.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

import static spark.Spark.*;

/**
 * Created by kirio on 29.10.2018.
 */
public class Server {


    private final static Logger LOG = LoggerFactory.getLogger(Server.class);


    public void init() {
        port(8080);
        initErrorHandlers();
        initAccountEndpoint();

    }
    private void initErrorHandlers() {
        ResponseTransformer json = JsonUtil.json();
        internalServerError((req, res) -> json.render(new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal error")));
        notFound((req, res) -> json.render(new ErrorMessage(HttpServletResponse.SC_NOT_FOUND, "not found error")));
    }

    private void initAccountEndpoint() {

       final String path = "account";

        before(String.format("/%s/*", path),
                (request, response) -> response.type("application/json"),

                (request, response) -> LOG.debug("{}?{}", request.uri(), request.queryString())
        );

        path(String.format("/%s", path), () -> {

            get("/new", this::newAccount, JsonUtil.json());


            get("/:id", this::accountInfo, JsonUtil.json());


            get("/:src_id/transfer_to/:dst_id", this::transfer, JsonUtil.json());

        });
    }

    private BigDecimal parseBigDecimalParameter(String paramName, String value) {
        if (value == null) {
            throw new RuntimeException(paramName + " parameter is required");
        }
        try {
            return new BigDecimal(value);
        } catch (Exception ex) {
            throw new RuntimeException(String.format("invalid %s parameter", paramName));

        }
    }


    private Object newAccount(Request request, Response responce) {
        try {
            BigDecimal balance = parseBigDecimalParameter("balance", request.queryParams("balance"));
            Account account = new Account(1, balance);
            return account;
        } catch (Exception ex) {
            responce.status(HttpServletResponse.SC_BAD_REQUEST);
            return new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        }
    }

    private Object accountInfo(Request request, Response response) {
        return null;
    }

    private Object transfer(Request request, Response responce) {
        BigDecimal amount;
        try {
            amount = parseBigDecimalParameter("amount", request.queryParams("amount"));
        } catch (Exception ex) {
            responce.status(HttpServletResponse.SC_BAD_REQUEST);
            return new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        }

        long srcId = Long.parseLong(request.params(":src_id"));
        long dstId = Long.parseLong(request.params(":dst_id"));

        return "GOOD" + srcId + " - " + dstId + " " + amount;
    }


}
