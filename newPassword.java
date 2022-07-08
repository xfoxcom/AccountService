package account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class newPassword {
    @NotEmpty
    @Size(min = 12, message = "The password length must be at least 12 chars!")
    private String new_password;
}
