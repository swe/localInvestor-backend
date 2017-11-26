package my.senik11.nordea.model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

import java.util.List;

/**
 * @author Arseny Krasenkov {@literal <akrasenkov@at-consulting.ru>}
 */
@Entity
public class CreditRequest {

    @Id
    private Long id;

    // fullfillments

    public List<CreditPayment> payments;

    public PendingCreditPayment nextPayment;

    public long amount;

    public long createdAt;

    public long termInWeeks;

    public long lastPaymentAt;

    public long creditPoints;

    public boolean started;

    public boolean finished;

    @Parent
    public Key<?> companyKey;

    public CreditRequest() {
    }
    public Key<?> getKey() {
        if (id == null || companyKey == null) {
            return null;
        }
        return Key.create(companyKey, CreditResponsePack.class, id);
    }
}
