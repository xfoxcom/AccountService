package account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


import javax.sql.DataSource;
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final String rolesQuery = "select email, role CASE WHEN role = 'ROLE_ADMINISTRATOR' THEN 'ROLE_ADMINISTRATOR'" +
            "WHEN role = 'ROLE_ACCOUNTANT' THEN 'ROLE_ACCOUNTANT'" +
            " ELSE 'ROLE_USER' END FROM roles;";

    @Autowired
    private DataSource dataSource;

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
                .and().exceptionHandling().accessDeniedHandler((request, response, accessDeniedException) -> response.sendError(403, "Access Denied!")).and()
                .csrf().disable().headers().frameOptions().disable().and().httpBasic();// no session
    }
    @Bean
    public PasswordEncoder encoder () {
        return new BCryptPasswordEncoder();
    }


}
