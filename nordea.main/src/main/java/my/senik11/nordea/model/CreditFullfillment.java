package my.senik11.nordea.model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

/**
 * @author Arseny Krasenkov {@literal <akrasenkov@at-consulting.ru>}
 */
@Entity
public class CreditFullfillment {

    @Id
    private Long id;

    @Parent
    public Key<?> requestKey;

    public long amount;

    public long createdAt;

    @Index
    public Key<?> by;

    public CreditFullfillment() {
    }

    public Key<?> getKey() {
        if (id == null || requestKey == null) {
            return null;
        }
        return Key.create(requestKey, CreditFullfillment.class, id);
    }
}
