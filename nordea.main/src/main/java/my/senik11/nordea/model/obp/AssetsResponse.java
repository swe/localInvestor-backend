package my.senik11.nordea.model.obp;

import java.util.List;

/**
 * @author Arseny Krasenkov {@literal <akrasenkov@at-consulting.ru>}
 */
public class AssetsResponse {

    public Object groupHeader;
    public ResponseContainer response;

    public static class GroupHeader {
        public String messageIdentification;
        public String creationDateTime;
        public int httpCpde;
    }

    public static class ResponseContainer {
        public List<Account> accounts;
    }

    public static class Account {
        public String currency;
        public String accountNumber;
        public String accountId;
        public List<String> permissions;
    }

    public List<Account> getAccounts() {
        return response.accounts;
    }

}
