package my.senik11.nordea.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * @author Arseny Krasenkov {@literal <akrasenkov@at-consulting.ru>}
 */
@Entity
public class Company {

    @Id
    private Long id;
    public String name;
    public String about;

    public String url;
    public String photoUrl;

    public Company() {
    }

    public Company(Long id, String name, String about, String url, String photoUrl) {
        this.id = id;
        this.name = name;
        this.about = about;
        this.url = url;
        this.photoUrl = photoUrl;
    }

}
