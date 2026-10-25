package com.chungnam.bidstats.config;

import com.chungnam.bidstats.service.AdminUserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final AdminUserService users; private final PasswordEncoder encoder;
    public SecurityConfig(AdminUserService users, PasswordEncoder encoder) { this.users = users; this.encoder = encoder; }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(users).passwordEncoder(encoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/admin/**", "/admin", "/h2-console/**").hasRole("ADMIN")
                .anyRequest().permitAll()
            .and().formLogin().loginPage("/login").defaultSuccessUrl("/admin", false).failureUrl("/login?error").permitAll()
            .and().logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/?logout").permitAll()
            .and().csrf().ignoringAntMatchers("/h2-console/**")
            .and().headers().frameOptions().sameOrigin();
    }
}
