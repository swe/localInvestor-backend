package my.senik11.nordea;

/**
 * @author Arseny Krasenkov {@literal <akrasenkov@at-consulting.ru>}
 */
public final class Urls {
    public static final String FB_AUTH_PATH = "https://www.facebook.com/v2.11/dialog/oauth?" +
            "  client_id=1518306524871721" +
            "  &redirect_uri=http://localhost:8080/auth";
    public static final String FB_TOKEN_DEBUG_TMPL = "https://graph.facebook.com/debug_token?" +
            "     input_token=%s" +
            "     &access_token=%s";
    public static final String FB_TOKEN_EXCHANGE_PATH_TMPL = "https://graph.facebook.com/v2.11/oauth/access_token?" +
            "   client_id=1518306524871721" +
            "   &redirect_uri=http://localhost:8080/auth" +
            "   &client_secret=cbb4ea118a55c4464f277322cd0842ed" +
            "   &code=%s";
    public static final String FB_PROFILE_PATH_TMPL = "https://graph.facebook.com/v2.11/%s";

    public static final String NORDEA_AUTH = "https://api.hackathon.developer.nordeaopenbanking.com/v1/" +
            "authentication?state=0&client_id=%s&redirect_uri=%s&X-Response-Scenarios=AuthenticationSkipUI";
    public static final String NORDEA_TOKEN_EXCHANGE_TMPL = "https://api.hackathon.developer.nordeaopenbanking.com/v1/" +
            "authentication/access_token?code=%s&redirect_uri=%s";
    public static final String NORDEA_ACCOUNT_ASSETS = "https://api.hackathon.developer.nordeaopenbanking.com/v1/assets";

    public static final String NORDEA_PAYMENT_INIT = "https://api.hackathon.developer.nordeaopenbanking.com/v2/payments/sepa";
    public static final String NORDEA_PAYMENT_INFO_TMPL = "https://api.hackathon.developer.nordeaopenbanking.com/v2/payments/sepa/%s";

    public static final String SELF_FRONT = "https://junction-tyzzo.appspot.com";
    public static final String SELF_FRONT_TMPL = SELF_FRONT + "?session-key=%s";
    public static final String SELF_API = "https://api-dot-junction-tyzzo.appspot.com";
    public static final String AUTH_SUCCESS = SELF_API + "/auth/success";

}
