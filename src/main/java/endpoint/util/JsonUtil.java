package endpoint.util;

import com.google.gson.Gson;
import spark.ResponseTransformer;

/**
 * Created by kirio on 29.10.2018.
 */
public class JsonUtil {
    private static class JsonTransformer implements ResponseTransformer {
        static private JsonTransformer INTANCE = new JsonTransformer();

        private final Gson gson = new Gson();

        @Override
        public String render(Object o) throws Exception {
            return gson.toJson(o);
        }
    }


    public static ResponseTransformer json() {
        return JsonTransformer.INTANCE;
    }
}
