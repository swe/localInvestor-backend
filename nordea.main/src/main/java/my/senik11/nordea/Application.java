package my.senik11.nordea;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyFilter;
import com.googlecode.objectify.ObjectifyService;
import my.senik11.nordea.config.KeyAdapter;
import my.senik11.nordea.model.Account;
import my.senik11.nordea.model.Company;
import my.senik11.nordea.model.CreditFullfillment;
import my.senik11.nordea.model.CreditRequest;
import my.senik11.nordea.model.FbUser;
import my.senik11.nordea.executions.AuthChallenge;
import my.senik11.nordea.executions.ProceedAuthExecution;
import my.senik11.nordea.model.PendingCreditPayment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.servlet.SparkApplication;
import spark.servlet.SparkFilter;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static my.senik11.nordea.model.Account.Type.COMPANY;
import static spark.Spark.*;

/**
 * @author Arseny Krasenkov {@literal <akrasenkov@at-consulting.ru>}
 */
public class Application extends GuiceServletContextListener implements SparkApplication {

    public static final String CLIENT_ID = "e15d9d65-ecc9-4d0e-b71f-d027f760746f";
    public static final String CLIENT_SECRET = "W4iN2hN3tU8eG0bO6tU1sS3rV7dA8gA7aV6lJ6hV2vW7yX4uS4";
    public static final String ATTR_ACCOUNT = "account";
    private final Logger log = LoggerFactory.getLogger(Application.class);
    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .registerTypeAdapter(Key.class, new KeyAdapter())
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private final ConcurrentHashMap<String, FbUser> sessions = new ConcurrentHashMap<>();
    private final HttpRequestFactory requestFactory = new UrlFetchTransport().createRequestFactory();

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new ComponentMappingModule(), new ServletMappingModule());
    }

    @Override
    public void init() {
        log.info("Service started successfully.");
        ObjectifyService.register(Account.class);
        ObjectifyService.register(Company.class);
        ObjectifyService.register(CreditRequest.class);
        ObjectifyService.register(CreditFullfillment.class);

        get("/auth/success", (request, response) -> new ProceedAuthExecution(request, response, requestFactory).execute());

        before("/*", (in, out) -> {
            out.header("Access-Control-Allow-Origin", "*");
            out.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            out.header("Access-Control-Allow-Credentials", "true");
        });
        before("/api/*", (request, response) -> new AuthChallenge(request, response, requestFactory).execute());

        get("/api/responses", (request, response) -> {
            Key<?> accountKey = request.attribute("account-key");
            Iterable<CreditFullfillment> creditResponses = ofy().load().type(CreditFullfillment.class)
                    .ancestor(accountKey).iterable();
            Map<Long, List<CreditFullfillment>> groupedResponses = new HashMap<>();
            for (CreditFullfillment creditResponse : creditResponses) {
                Long createdAt = creditResponse.createdAt;
                if (!groupedResponses.containsKey(createdAt)) {
                    groupedResponses.put(createdAt, Collections.singletonList(creditResponse));
                } else {
                    groupedResponses.get(createdAt).add(creditResponse);
                }
            }
            return gson.toJson(creditResponses);
        });
        post("/api/responses", (request, response) -> {
            String raw = request.body();
            List<CreditFullfillment> responses = gson.fromJson(raw, new TypeToken<List<CreditFullfillment>>() {}.getType());

            Account account = request.attribute(ATTR_ACCOUNT);
            long fullAmonut = responses.stream()
                    .map((r) -> r.amount)
                    .reduce(Long::sum)
                    .orElseThrow(() -> halt(400, "invalid-amount"));
            String accessToken = account.accessToken;
            String accountId = account.bankAccountId;

            GenericUrl paymentInitUrl = new GenericUrl(Urls.NORDEA_PAYMENT_INIT);
            HttpHeaders requestHeaders = new HttpHeaders()
                    .set("X-IBM-Client-Id", CLIENT_ID)
                    .set("X-Response-Scenarios", "AuthorizationSkipAccessControl")
                    .set("Authorization", "Bearer " + accessToken)
                    .set("X-IBM-Client-Secret", CLIENT_SECRET);
            JsonObject creditorAccountObject = new JsonObject();
            creditorAccountObject.addProperty("value", "FI112233442");
            creditorAccountObject.addProperty("_type", "IBAN");

            JsonObject creditorObject = new JsonObject();
            creditorObject.add("account", creditorAccountObject);
            creditorObject.addProperty("name", account.firstName + " " + account.lastName);
            creditorObject.addProperty("message", "credit response payment");

            JsonObject debtorObject = new JsonObject();
            debtorObject.addProperty("_accountId", accountId);

            JsonObject paymentObject = new JsonObject();
            paymentObject.addProperty("amount", fullAmonut);
            paymentObject.addProperty("currency", "EUR");
            paymentObject.add("debtor", debtorObject);
            paymentObject.add("creditor", creditorObject);

            HttpResponse paymentResponse = requestFactory.buildPostRequest(paymentInitUrl,
                    new JsonHttpContent(new GsonFactory(), paymentObject))
                    .execute();
            if (paymentResponse.getStatusCode() == 201) {
                ofy().save().entities(responses);
            } else {
                halt(400, "payment-failed");
            }

            ofy().save().entities(responses);
            response.status(201);
            return null;
        });

        get("/api/requests", (request, response) -> {
            List<CreditRequest> requests = ofy().load().type(CreditRequest.class).list();

            return gson.toJson(requests);
        });
        post("/api/:companyKey/requests", (in, out) -> {
            Account account = in.attribute("account");
            if (account.type != COMPANY) {
                throw halt(400, "not-company");
            }
            Map<String, Long> requestDetails = gson.fromJson(in.body(), new TypeToken<Map<String, Long>>() {}.getType());
            long amount = requestDetails.get("amount");
            long interest = requestDetails.get("interest");
            long nextPaymentInWeeks = requestDetails.get("nextPaymentInWeeks");
            long termInWeeks = requestDetails.get("termInWeeks");

            CreditRequest creditRequest = new CreditRequest();
            creditRequest.payments = emptyList();
            creditRequest.nextPayment = new PendingCreditPayment();
            creditRequest.nextPayment.amount = amount;
            creditRequest.nextPayment.interest = interest;
            creditRequest.nextPayment.date = nextPaymentInWeeks;
            creditRequest.createdAt = System.currentTimeMillis();
            creditRequest.termInWeeks = termInWeeks;
            creditRequest.lastPaymentAt = 0L;
            creditRequest.creditPoints = 0;
            creditRequest.started = false;
            creditRequest.finished = false;
            creditRequest.companyKey = Key.create(in.params("companyKey"));

            ofy().save().entities(creditRequest).now();

            out.status(201);
            return null;
        });


    }


    @Override
    public void destroy() {
        log.info("Service stopped successfully.");
    }

    public static final class ComponentMappingModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ObjectifyFilter.class).in(Singleton.class);
            bind(SparkFilter.class).in(Singleton.class);
        }
    }

    public static final class ServletMappingModule extends ServletModule {
        @Override
        protected void configureServlets() {
            Map<String, String> sparkParams = new TreeMap<>();
            sparkParams.put("applicationClass", Application.class.getName());
            filter("/*").through(ObjectifyFilter.class);
            filter("/*").through(SparkFilter.class, sparkParams);
        }
    }

}
