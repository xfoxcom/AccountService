package account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Locale;

@Component
public class loggerController {
    private final static Logger logger = LoggerFactory.getLogger(loggerController.class);
    @Autowired
    private eventsRepository events;

    public loggerController (eventsRepository events) {
        this.events = events;
    }

    public void create_user (String email) {
        logger.info("User created.");
        eventField field = new eventField();

        field.setDate(LocalDate.now());
        field.setAction("CREATE_USER");
        field.setSubject("Anonymous");
        field.setObject(email);
        field.setPath("/api/auth/signup");

        events.save(field);
    }

    public void change_password (String email) {

        logger.info("Password changed.");
        eventField field = new eventField();

        field.setDate(LocalDate.now());
        field.setAction("CHANGE_PASSWORD");
        field.setSubject(email.toLowerCase(Locale.ROOT));
        field.setObject(email.toLowerCase(Locale.ROOT));
        field.setPath("/api/auth/changepass");

        events.save(field);
    }

    public void grant_role (String emailSub, String emailObj, String role) {
        logger.info("Role granted.");

        eventField field = new eventField();

        field.setDate(LocalDate.now());
        field.setAction("GRANT_ROLE");
        field.setSubject(emailSub.toLowerCase(Locale.ROOT));
        field.setObject("Grant role " + role + " to " + emailObj.toLowerCase(Locale.ROOT));
        field.setPath("/api/admin/user/role");

        events.save(field);
    }

    public void remove_role (String emailSub, String emailObj, String role) {
        logger.info("Role removed.");

        eventField field = new eventField();

        field.setDate(LocalDate.now());
        field.setAction("REMOVE_ROLE");
        field.setSubject(emailSub.toLowerCase(Locale.ROOT));
        field.setObject("Remove role " + role + " from " + emailObj.toLowerCase(Locale.ROOT));
        field.setPath("/api/admin/user/role");

        events.save(field);
    }

    public void lockUser (String emailSub, String emailObj, String path) {
        logger.info("User locked.");

        eventField field = new eventField();

        field.setDate(LocalDate.now());
        field.setAction("LOCK_USER");
        field.setSubject(emailSub.toLowerCase(Locale.ROOT));
        field.setObject("Lock user " + emailObj);
        field.setPath(path);

        events.save(field);
    }

    public void unlockUser (String emailSub, String emailObj) {
        logger.info("User unlocked.");

        eventField field = new eventField();

        field.setDate(LocalDate.now());
        field.setAction("UNLOCK_USER");
        field.setSubject(emailSub.toLowerCase(Locale.ROOT));
        field.setObject("Unlock user " + emailObj.toLowerCase(Locale.ROOT));
        field.setPath("/api/admin/user/access");

        events.save(field);

    }

    public void deleteUser (String emailSub, String emailObj) {
        logger.info("User deleted.");

        eventField field = new eventField();

        field.setDate(LocalDate.now());
        field.setAction("DELETE_USER");
        field.setSubject(emailSub.toLowerCase(Locale.ROOT));
        field.setObject(emailObj.toLowerCase(Locale.ROOT));
        field.setPath("/api/admin/user");

        events.save(field);

    }

    public void loginFailed (String emailSub, String path) {
        logger.info("Log in failed.");

        eventField field = new eventField();

        field.setDate(LocalDate.now());
        field.setAction("LOGIN_FAILED");
        field.setSubject(emailSub.toLowerCase(Locale.ROOT));
        field.setObject(path);
        field.setPath(path);

        events.save(field);
    }

    public void accessDenied (String emailSub, String path) {
        logger.info("Access denied.");

        eventField field = new eventField();

        field.setDate(LocalDate.now());
        field.setAction("ACCESS_DENIED");
        field.setSubject(emailSub.toLowerCase(Locale.ROOT));
        field.setObject(path);
        field.setPath(path);

        events.save(field);
    }

    public void bruteForce (String emailSub, String path) {
        logger.info("Brute force.");

        eventField field = new eventField();

        field.setDate(LocalDate.now());
        field.setAction("BRUTE_FORCE");
        field.setSubject(emailSub.toLowerCase(Locale.ROOT));
        field.setObject(path);
        field.setPath(path);

        events.save(field);
    }

    // TODO: 14.07.2022 Раскидать 3 метода и лок юзера

}
