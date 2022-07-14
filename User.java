package account;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotEmpty
    private String name;
    @NotEmpty
    private String lastname;
    @NotEmpty
    @Pattern(regexp = ".+@acme.com")
    private String email;
    @NotEmpty
    @Size(min = 12, message = "The password length must be at least 12 chars!")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean enable;
    @ElementCollection()
    private List<String> roles = new ArrayList<>();
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String authority;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean locked;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int failedAttempt;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Date lockTime;


    public void setAuthority(List<String> roles) {
        this.authority = roles.get(0);
    }

    public void sortList () {
        roles.sort(Comparator.naturalOrder());
    }
}
