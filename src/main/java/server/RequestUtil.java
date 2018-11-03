package server;

import model.exception.BadParamException;
import spark.Request;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

/**
 * Created by kirio on 31.10.2018.
 */
public interface RequestUtil {
    static BigDecimal bigDecimalQueryParam(@Nonnull Request request, @Nonnull String paramName) throws BadParamException {
        String value = request.queryParams(paramName);
        if (value == null) {
            throw new BadParamException(paramName + " parameter is required");
        }
        try {
            return new BigDecimal(value);
        } catch (Exception ex) {
            throw new BadParamException(String.format("invalid %s parameter", paramName));
        }
    }

    static Long longPathParam(@Nonnull Request request, @Nonnull String paramName) throws BadParamException {
        String value = request.params(paramName);
        if (value == null) {
            throw new BadParamException("path param " + paramName + " is undefined");
        }
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            throw new BadParamException("path param " + paramName + " is invalid");
        }
    }
}
