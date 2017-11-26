package my.senik11.nordea.executions;

import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import my.senik11.nordea.Application;
import my.senik11.nordea.Urls;
import my.senik11.nordea.model.Account;
import my.senik11.nordea.model.obp.TokenExchangeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static java.util.Collections.singletonList;
import static spark.Spark.halt;

/**
 * @author Arseny Krasenkov
 */
public class ProceedAuthExecution extends Execution {

    private static final Logger log = LoggerFactory.getLogger(ProceedAuthExecution.class);

    public ProceedAuthExecution(Request request, Response response, HttpRequestFactory requestFactory) {
        super(request, response, requestFactory);
    }

    @Override
    public String execute() {
        String accessCode = request.queryParamOrDefault("code", null);
        if (accessCode == null) {
            fail("no-code");
        }

        String bankAccountId = null;
        String accessToken = null;
        try {
            GenericUrl exchangeUrl = new GenericUrl(String.format(Urls.NORDEA_TOKEN_EXCHANGE_TMPL, accessCode, Urls.AUTH_SUCCESS));
            HttpHeaders requestHeaders = new HttpHeaders()
                    .set("X-IBM-Client-Id", Application.CLIENT_ID)
                    .set("Content-Type", singletonList("application/x-www-form-urlencoded"))
                    .set("X-IBM-Client-Secret", Application.CLIENT_SECRET);
            HttpResponse exchangeResponse = requestFactory.buildPostRequest(exchangeUrl, new EmptyContent())
                    .setHeaders(requestHeaders)
                    .execute();

            int responseStatus = exchangeResponse.getStatusCode();
            if (responseStatus == 403 || responseStatus == 401) {
                fail("nordea-forbidden");
            }

            String exchangeDataRaw = exchangeResponse.parseAsString();
            accessToken = gson.fromJson(exchangeDataRaw, TokenExchangeResponse.class).accessToken;
        } catch (IOException ioex) {
            log.error("Exception during token exchange: ", ioex);
            fail("io-exception");
        }

        try {
            HttpHeaders requestHeaders = new HttpHeaders().set("Content-Type", singletonList("application/json"));
            GenericUrl assetsUrl = new GenericUrl("https://api.hackathon.developer.nordeaopenbanking.com/v1/assets");
            HttpResponse assetsResponse = requestFactory.buildGetRequest(assetsUrl)
                    .setHeaders(requestHeaders)
                    .execute();
            JsonObject assetsObject = new JsonParser().parse(assetsResponse.parseAsString()).getAsJsonObject();
            JsonArray accountsArray = assetsObject.getAsJsonObject("accounts").getAsJsonArray();
            JsonObject firstAccount = accountsArray.get(0).getAsJsonObject();
            bankAccountId = firstAccount.getAsJsonPrimitive("accountId").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String sessionKey = UUID.randomUUID().toString();
        long sessionExpiresAt = System.currentTimeMillis() + 36000L;

        Account account = ofy().load().type(Account.class).filter("bankAccountId", bankAccountId).first().now();
        if (account == null) {
            account = new Account();
            account.accessToken = accessToken;
            account.bankAccountId = bankAccountId;
            account.firstName = "Bob";
            account.lastName = "Test";
        }
        account.sessionExpiresAt = sessionExpiresAt;
        account.sessionKey = sessionKey;

        ofy().save().entity(account).now();

        response.redirect(String.format(Urls.SELF_FRONT_TMPL, sessionKey));
        return null;
    }

    @Override
    protected void fail(String message) {
        log.info("Failed: " + message);

        Map<String, String> failData = new HashMap<>();
        failData.put("message", message);
        failData.put("redirect", String.format(Urls.NORDEA_AUTH, Application.CLIENT_ID, Urls.AUTH_SUCCESS));
        halt(401, gson.toJson(failData));
    }
}
