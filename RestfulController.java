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
import java.util.*;

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
    @Autowired
    private authoritiesRepository authoritiesRepository;
    @Autowired
    private loggerController logger;
    @Autowired
    private eventsRepository events;

    public RestfulController (UserRepository userRepository, employeeRepository employeeRepository, authoritiesRepository authoritiesRepository, eventsRepository events) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.authoritiesRepository = authoritiesRepository;
        this.events = events;
    }

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
user.setLocked(false);
user.setFailedAttempt(0);
user.setEmail(user.getEmail().toLowerCase(Locale.ROOT));
user.setPassword(new BCryptPasswordEncoder(13).encode(user.getPassword()));
userRepository.save(user);
logger.create_user(user.getEmail());
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
user.setFailedAttempt(0);
userRepository.save(user);
logger.change_password(auth.getName());
return new ResponsePasswordChange(auth.getName(), "The password has been updated successfully");
    }

    @RolesAllowed({"ROLE_ACCOUNTANT"})
    @PostMapping("api/acct/payments")
    @Transactional
    public ResponseEntity<Map<String, String>> addSalary (@RequestBody(required = false) List<Employee> employees, Authentication auth) {
        User user = userRepository.findByEmailIgnoreCase(auth.getName());
        if (user.isLocked()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is locked!");
        }
        user.setFailedAttempt(0);
        userRepository.save(user);

        for (Employee employee : employees) {
            int month = Integer.parseInt(employee.getPeriod().split("-")[0]);
if (!userRepository.existsByEmailIgnoreCase(employee.getEmployee())) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "We don't have this user!");
}
if (employeeRepository.existsByPeriodAndEmployee(employee.getPeriod(), employee.getEmployee())) {
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
    public ResponseEntity<Map<String, String>> updateSalary (@RequestBody @Valid Employee employee, Authentication auth) {
        User user = userRepository.findByEmailIgnoreCase(auth.getName());
        if (user.isLocked()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is locked!");
        }
        user.setFailedAttempt(0);
        userRepository.save(user);
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
    public <T> T getSalaryByPeriod (@RequestParam(required = false) String period, Authentication auth) {
        User user = userRepository.findByEmailIgnoreCase(auth.getName());
        if (user.isLocked()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User account is locked");
        }
        user.setFailedAttempt(0);
        userRepository.save(user);
        List<ClassForPayrollResponse> list = new ArrayList<>();
        if (period == null) {
            List<Employee> employees = employeeRepository.findByEmployeeOrderByPeriodDesc(auth.getName());
            for (Employee employee : employees) {
                ClassForPayrollResponse classForPayrollResponse = new ClassForPayrollResponse();
                classForPayrollResponse.setName(user.getName());
                classForPayrollResponse.setLastname(user.getLastname());
                classForPayrollResponse.setPeriod(employee.getPeriod());
                classForPayrollResponse.setSalary(employee.getSalary());
                list.add(classForPayrollResponse);
            }
            return (T) list;
        }
        int month = Integer.parseInt(period.split("-")[0]);
        if (month < 0 | month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong date!");
        }
        String email = auth.getName();
Employee employee = employeeRepository.findByPeriodAndEmployee(period, email);
ClassForPayrollResponse worker = new ClassForPayrollResponse();
worker.setName(user.getName());
worker.setLastname(user.getLastname());
worker.setPeriod(period);
worker.setSalary(employee.getSalary());
list.add(worker);
return (T) worker;
    }

    @GetMapping("api/admin/user")
    @RolesAllowed({"ROLE_ADMINISTRATOR"})
    public List<User> getAllUsers () {
        return userRepository.findAll();
    }
    @DeleteMapping("/api/admin/user/{email}")
    //@RolesAllowed({"ROLE_ADMINISTRATOR"})
    public Object deleteUser (@PathVariable String email, Authentication auth) {
        User user = userRepository.findByEmailIgnoreCase(auth.getName());
        user.setFailedAttempt(0);
        userRepository.save(user);
if (!userRepository.existsByEmailIgnoreCase(email)) {
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
}
if (userRepository.findByEmailIgnoreCase(email).getAuthority().equals("ROLE_ADMINISTRATOR")) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
}
userRepository.delete(userRepository.findByEmailIgnoreCase(email));
class deleteResponse {
    public String user;
    public String status;
    public deleteResponse (String user, String status) {
        this.user = user;
        this.status = status;
    }
}
logger.deleteUser(auth.getName(), email);
return new deleteResponse(email, "Deleted successfully!");
    }
    @PutMapping("api/admin/user/role")
    @RolesAllowed({"ROLE_ADMINISTRATOR"})
    public User putRole (@RequestBody putRequest putRequest, Authentication auth) {
        User user1 = userRepository.findByEmailIgnoreCase(auth.getName());
        user1.setFailedAttempt(0);
        userRepository.save(user1);
        if (!userRepository.existsByEmailIgnoreCase(putRequest.getUser())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }
        if (!putRequest.getRole().equals("ACCOUNTANT") & !putRequest.getRole().equals("USER") & !putRequest.getRole().equals("ADMINISTRATOR") & !putRequest.getRole().equals("AUDITOR") ) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!");
        }

        if (putRequest.getOperation().equals("GRANT")) {
            if (userRepository.findByEmailIgnoreCase(putRequest.getUser()).getAuthority().equals("ROLE_ADMINISTRATOR") & (putRequest.getRole().equals("USER") | putRequest.getRole().equals("AUDITOR"))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user cannot combine administrative and business roles!");
            }
            if ((userRepository.findByEmailIgnoreCase(putRequest.getUser()).getAuthority().equals("ROLE_USER") | userRepository.findByEmailIgnoreCase(putRequest.getUser()).getAuthority().equals("ROLE_ACCOUNTANT"))
            & putRequest.getRole().equals("ADMINISTRATOR")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user cannot combine administrative and business roles!");
            }

            User user = userRepository.findByEmailIgnoreCase(putRequest.getUser());
            List<String> roles = userRepository.findByEmailIgnoreCase(putRequest.getUser()).getRoles();
            roles.add("ROLE_" + putRequest.getRole());
            user.sortList();
            user.setAuthority(roles);
            userRepository.save(user);
            logger.grant_role(auth.getName(), putRequest.getUser(), putRequest.getRole());
            return user;
        }

        if (putRequest.getOperation().equals("REMOVE")) {

            if (userRepository.findByEmailIgnoreCase(putRequest.getUser()).getAuthority().equals("ROLE_ADMINISTRATOR")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
            }

            if (!userRepository.findByEmailIgnoreCase(putRequest.getUser()).getRoles().contains("ROLE_" + putRequest.getRole())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have a role!");
            }
            if (userRepository.findByEmailIgnoreCase(putRequest.getUser()).getRoles().size() == 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");
            }

            User user = userRepository.findByEmailIgnoreCase(putRequest.getUser());
            user.getRoles().remove("ROLE_" + putRequest.getRole());
            user.setAuthority(user.getRoles());
            userRepository.save(user);
            logger.remove_role(auth.getName(), putRequest.getUser(), putRequest.getRole());
            return user;
        }
        return new User();
    }
    @PutMapping("api/admin/user/access")
    public Map<String, String> lockOrUnlockUser (@RequestBody userOperationRequest operation, Authentication auth) {
        User user1 = userRepository.findByEmailIgnoreCase(auth.getName());
        user1.setFailedAttempt(0);
        userRepository.save(user1);
        if (userRepository.findByEmailIgnoreCase(operation.getUser()).getAuthority().equals("ROLE_ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
        }
        if (operation.getOperation().equals("LOCK")) {
            User user = userRepository.findByEmailIgnoreCase(operation.getUser());
            user.setLocked(true);
            userRepository.save(user);
            logger.lockUser(auth.getName(), operation.getUser(), "api/admin/user/access");
            return Map.of("status", "User " + operation.getUser().toLowerCase(Locale.ROOT) + " locked!");
        }
        User user = userRepository.findByEmailIgnoreCase(operation.getUser());
        user.setLocked(false);
        user.setFailedAttempt(0);
        userRepository.save(user);
        logger.unlockUser(auth.getName(), operation.getUser());
        return Map.of("status", "User " + operation.getUser().toLowerCase(Locale.ROOT) + " unlocked!");
    }
    @GetMapping("/api/security/events")
    public List<eventField> getEvents () {
List<eventField> list = events.findAll();
list.sort(Comparator.comparing(eventField::getId));
return list;
    }
}
