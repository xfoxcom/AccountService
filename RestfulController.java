package account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
public class RestfulController {
    @Autowired
    private UserRepository userRepository;
    @PostMapping("api/auth/signup")
    public User signup (@RequestBody @Valid User user) {
if (userRepository.existsByEmail(user.getEmail())) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
}
user.setEnable(true);
user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
userRepository.save(user);
        return user;
    }
    @GetMapping("api/empl/payment")
    public User testAuth (Authentication auth) {
        String email = auth.getName();
return userRepository.findByEmailIgnoreCase(email);
    }
}
