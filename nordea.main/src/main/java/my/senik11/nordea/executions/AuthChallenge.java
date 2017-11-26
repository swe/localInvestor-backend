package my.senik11.nordea.executions;

import com.google.api.client.http.HttpRequestFactory;
import my.senik11.nordea.Application;
import my.senik11.nordea.Urls;
import my.senik11.nordea.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static spark.Spark.halt;

/**
 * @author Arseny Krasenkov {@literal <akrasenkov@at-consulting.ru>}
 */
public class AuthChallenge extends Execution {
    private static final Logger log = LoggerFactory.getLogger(ProceedAuthExecution.class);

    public AuthChallenge(Request request, Response response, HttpRequestFactory requestFactory) {
        super(request, response, requestFactory);
    }

    @Override
    public String execute() {
        String sessionKey = request.queryParams(SESSION_KEY_PARAM);
        if (StringUtils.isEmpty(sessionKey)) {
            fail("no-cookie");
        }

        Account account = ofy().load().type(Account.class).filter("sessionKey", sessionKey).first().now();
        if (account == null) {
            fail("no-account");
        }

        request.attribute(Application.ATTR_ACCOUNT, account);
        return null;
    }

    @Override
    protected void fail(String message) {
        log.debug("Failed: " + message);

        Map<String, String> failData = new HashMap<>();
        failData.put("message", message);
        failData.put("redirect", String.format(Urls.NORDEA_AUTH, Application.CLIENT_ID, Urls.AUTH_SUCCESS));
        response.status(401);
        response.body(gson.toJson(failData));
        halt(401, gson.toJson(failData));
    }


}
