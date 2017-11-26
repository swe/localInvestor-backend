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
public class CreditResponsePack {

    @Id
    private Long id;

    @Parent
    public Key<?> accountKey;

    @Index
    public Key<?> requestKey;

    public long amount;

    public long createdAt;

    public CreditResponsePack() {
    }

    public Key<?> getKey() {
        if (id == null || accountKey == null) {
            return null;
        }
        return Key.create(accountKey, CreditResponsePack.class, id);
    }
}
