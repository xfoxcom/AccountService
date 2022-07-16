package account;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final String rolesQuery = "select email, role CASE WHEN role = 'ROLE_ADMINISTRATOR' THEN 'ROLE_ADMINISTRATOR'" +
            "WHEN role = 'ROLE_ACCOUNTANT' THEN 'ROLE_ACCOUNTANT'" +
            " ELSE 'ROLE_USER' END FROM roles;";

    @Autowired
    private DataSource dataSource;
    @Autowired
    private loggerController logger;
    @Autowired
    private UserRepository users;

    public WebSecurityConfig (UserRepository users) {
        this.users = users;
    }

    @Override
    protected void configure (AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .usersByUsernameQuery("select email, password, 'true' as enable from users where email=lower(?)")
                .authoritiesByUsernameQuery("select email, authority from users where email =?")
                .passwordEncoder(new BCryptPasswordEncoder());
    }

    @Override
    protected void configure (HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                .mvcMatchers("api/acct/payments").permitAll()
                .mvcMatchers("/api/admin/user/**").hasRole("ADMINISTRATOR")
                .mvcMatchers("/api/acct/payments").hasRole("ACCOUNTANT")
                .mvcMatchers("/api/security/events").hasRole("AUDITOR")
                .mvcMatchers("api/auth/changepass").authenticated()
                .mvcMatchers("api/empl/payment").authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling().accessDeniedHandler((request, response, accessDeniedException) -> {
            response.sendError(403, "Access Denied!");
            logger.accessDenied(request.getUserPrincipal().getName(), request.getRequestURI());
        }).and().csrf().disable().headers().frameOptions().disable().and().httpBasic().authenticationEntryPoint((request, response, authException) -> {

                    String encoded = request.getHeader("authorization");
                    String emailAndPass = new String(Base64.decodeBase64(encoded.split(" ")[1].getBytes()));
                    String email = emailAndPass.split(":")[0];

            User user = users.findByEmailIgnoreCase(email);
            user.setFailedAttempt(user.getFailedAttempt() + 1);
            if (user.getFailedAttempt() > 5) {
                user.setLocked(true);
                logger.bruteForce(email, request.getRequestURI());
            } else {
                users.save(user);
                logger.loginFailed(email, request.getRequestURI());
            }
            response.sendError(401, "Unauthorized");

        });
    }
    @Bean
    public PasswordEncoder encoder () {
        return new BCryptPasswordEncoder();
    }
}
