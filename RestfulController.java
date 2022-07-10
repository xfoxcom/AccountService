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

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.util.ArrayList;
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
if (userRepository.count() == 0) {
    List<String> roles = user.getRoles();
    roles.add("ROLE_ADMINISTRATOR");
    user.setAuthority(roles);
} else {
    List<String> roles = user.getRoles();
    roles.add("ROLE_USER");
    user.setAuthority(roles);
}
user.setEnable(true);
user.setEmail(user.getEmail().toLowerCase(Locale.ROOT));
user.setPassword(new BCryptPasswordEncoder(13).encode(user.getPassword()));
userRepository.save(user);
        return user;
    }

    @RolesAllowed({"ROLE_USER", "ROLE_ACCOUNTANT", "ROLE_ADMINISTRATOR"})
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

    @RolesAllowed({"ROLE_ACCOUNTANT"})
    @PostMapping("api/acct/payments")
    @Transactional
    public ResponseEntity<Map<String, String>> addSalary (@RequestBody List<Employee> employees) {

        for (Employee employee : employees) {
            int month = Integer.parseInt(employee.getPeriod().split("-")[0]);
if (!userRepository.existsByEmailIgnoreCase(employee.getEmployee())) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "We don't have this user!");
}
if (employeeRepository.existsByPeriodAndEmployee(employee.getPeriod(), employee.getEmployee())) {   // TODO: 09.07.2022 period and email
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong date!");
}
if (employee.getSalary() < 0) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Salary must be non negative!");
}
if (month < 0 | month > 12) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong date!");
}
            employeeRepository.save(employee);
        }

        return ResponseEntity.ok(Map.of("status", "Added successfully!"));
    }

    @PutMapping("api/acct/payments")
    @RolesAllowed({"ROLE_ACCOUNTANT"})
    public ResponseEntity<Map<String, String>> updateSalary (@RequestBody @Valid Employee employee) {
        if (!userRepository.existsByEmailIgnoreCase(employee.getEmployee())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        int month = Integer.parseInt(employee.getPeriod().split("-")[0]);
        if (month < 0 | month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong date!");
        }
        Employee worker = employeeRepository.findByPeriodAndEmployee(employee.getPeriod(), employee.getEmployee());
        worker.setSalary(employee.getSalary());

        employeeRepository.save(worker);
        return ResponseEntity.ok(Map.of("status", "Updated successfully!"));
    }

    @RolesAllowed({"ROLE_USER", "ROLE_ACCOUNTANT"})
    @GetMapping("api/empl/payment")
    public List<ClassForPayrollResponse> getSalaryByPeriod (@RequestParam(required = false) String period, Authentication auth) {
        List<ClassForPayrollResponse> list = new ArrayList<>();
        if (period == null) {
            User user = userRepository.findByEmailIgnoreCase(auth.getName());
            List<Employee> employees = employeeRepository.findByEmployeeOrderByPeriodDesc(auth.getName());

            for (Employee employee : employees) {
                ClassForPayrollResponse classForPayrollResponse = new ClassForPayrollResponse();
                classForPayrollResponse.setName(user.getName());
                classForPayrollResponse.setLastname(user.getLastname());
                classForPayrollResponse.setPeriod(employee.getPeriod());
                classForPayrollResponse.setSalary(employee.getSalary());
                list.add(classForPayrollResponse);
            }
            return list;
        }
        int month = Integer.parseInt(period.split("-")[0]);
        if (month < 0 | month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong date!");
        }
        String email = auth.getName();
User user = userRepository.findByEmailIgnoreCase(email);
Employee employee = employeeRepository.findByPeriodAndEmployee(period, email);
ClassForPayrollResponse worker = new ClassForPayrollResponse();
worker.setName(user.getName());
worker.setLastname(user.getLastname());
worker.setPeriod(period);
worker.setSalary(employee.getSalary());
list.add(worker);
return list;
    }

    @GetMapping("api/admin/user")
    @RolesAllowed({"ROLE_ADMINISTRATOR"})
    public List<User> getAllUsers () {
        return userRepository.findAll();
    }
    @DeleteMapping("/api/admin/user/{email}")
    @RolesAllowed({"ROLE_ADMINISTRATOR"})
    public void deleteUser (@PathVariable String email) {
if (!userRepository.existsByEmailIgnoreCase(email)) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found!");
}
userRepository.deleteByEmail(email);


    }
}
