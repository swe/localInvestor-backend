package my.senik11.nordea.model.obp;

/**
 * @author Arseny Krasenkov
 */
public class TokenExchangeResponse {

    public String accessToken;
    public int expiresIn;
    public String tokenType;

    public TokenExchangeResponse() {
    }

    public TokenExchangeResponse(String accessToken, int expiresIn, String tokenType) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.tokenType = tokenType;
    }
}
