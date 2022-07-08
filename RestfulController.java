package account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
public class RestfulController {
    private List<String> hackedPasswords = List.of("PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
            "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private employeeRepository employeeRepository;
    @PostMapping("api/auth/signup")
    public User signup (@RequestBody @Valid User user) {
if (userRepository.existsByEmailIgnoreCase(user.getEmail())) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User exist!");
}
if (hackedPasswords.contains(user.getPassword())) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
        }
user.setEnable(true);
user.setEmail(user.getEmail().toLowerCase(Locale.ROOT));
user.setPassword(new BCryptPasswordEncoder(13).encode(user.getPassword()));
userRepository.save(user);
        return user;
    }
    @GetMapping("api/empl/payment")
    public User testAuth (Authentication auth) {
        String email = auth.getName();
return userRepository.findByEmailIgnoreCase(email);
    }
    @PostMapping("api/auth/changepass")
    public ResponsePasswordChange changePassword (Authentication auth, @RequestBody newPassword newPassword) {
        if (newPassword.getNew_password().length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password length must be 12 chars minimum!");
        }
if (encoder.matches(newPassword.getNew_password(), userRepository.findByEmailIgnoreCase(auth.getName()).getPassword())) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");
}
if (hackedPasswords.contains(newPassword.getNew_password())) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
}
User user = userRepository.findByEmailIgnoreCase(auth.getName());
user.setPassword(encoder.encode(newPassword.getNew_password()));
userRepository.save(user);
return new ResponsePasswordChange(auth.getName(), "The password has been updated successfully");
    }

    @PostMapping("api/acct/payments")
    @Transactional
    public ResponseEntity<Map<String, String>> addSalary (@RequestBody List<@Valid Employee> employees) {

        for (Employee employee : employees) {

            employeeRepository.save(employee);
        }

        return ResponseEntity.ok(Map.of("status", "Added successfully!"));
    }

    @PutMapping("api/acct/payments")
    public ResponseEntity<Map<String, String>> updateSalary (@RequestBody @Valid Employee employee) {
        employeeRepository.save(employee);
        return ResponseEntity.ok(Map.of("status", "Updated successfully!"));
    }

    @GetMapping("api/empl/payment")
    public void getSalaryByPeriod (@RequestParam String period, Authentication auth) {




    }
}
