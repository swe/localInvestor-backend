package my.senik11.nordea.model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;

/**
 * @author Arseny Krasenkov
 */
@Entity
public class Account implements Serializable {

    public enum Type {
        COMPANY,
        INVESTOR;
    }

    @Id
    private Long id;
    public Type type;
    public String accessToken;
    public String firstName;
    public String lastName;

    @Index
    public String sessionKey;
    public String bankAccountId;
    public long sessionExpiresAt;

    public Account() {
    }

    public Key<?> getKey() {
        if (id == null) {
            return null;
        }
        return Key.create(Account.class, id);
    }
}
