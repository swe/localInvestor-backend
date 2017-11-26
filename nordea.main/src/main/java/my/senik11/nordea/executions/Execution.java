package my.senik11.nordea.executions;

import com.google.api.client.http.HttpRequestFactory;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.objectify.Key;
import my.senik11.nordea.config.KeyAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

/**
 * @author Arseny Krasenkov {@literal <akrasenkov@at-consulting.ru>}
 */
public abstract class Execution {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected static final Gson gson = new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .registerTypeAdapter(Key.class, new KeyAdapter())
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public static final String SESSION_KEY_PARAM = "session-key";

    protected final Request request;
    protected final Response response;
    protected final HttpRequestFactory requestFactory;

    public Execution(Request request, Response response, HttpRequestFactory requestFactory) {
        this.request = request;
        this.response = response;
        this.requestFactory = requestFactory;
    }

    public abstract String execute();

    protected void fail(String message) {
        log.info("Failed: " + message);
    }
}
