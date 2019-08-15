package de.warhog.fpvlaptracker.configuration;

import de.warhog.fpvlaptracker.db.JooqPersistentTokenRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private JooqPersistentTokenRepositoryImpl persistentTokenRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .logout().logoutSuccessUrl("/").and()
                .httpBasic().and()
                .authorizeRequests()
                .antMatchers("/api/auth/**", "/user").authenticated()
                .anyRequest().permitAll()
                .and()
                .cors()
                .and()
                .rememberMe()
                .tokenRepository(persistentTokenRepository)
                .key("remember-me-flt-secret-key")
                .rememberMeCookieName("remember-me-flt")
                .tokenValiditySeconds(30 * 24 * 60 * 60)
                .and()
                .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("admin").password("{noop}" + applicationConfig.getAdminPassword()).roles("ADMIN", "USER");
    }

}
