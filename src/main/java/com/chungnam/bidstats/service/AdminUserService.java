package com.chungnam.bidstats.service;

import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService implements UserDetailsService {
    private final JdbcTemplate jdbc; private final PasswordEncoder encoder;
    @Value("${admin.username:admin}") private String defaultUser;
    @Value("${admin.password:admin123}") private String defaultPass;
    public AdminUserService(JdbcTemplate jdbc, PasswordEncoder encoder) { this.jdbc = jdbc; this.encoder = encoder; }

    @PostConstruct
    public void seed() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM admin_user", Integer.class);
        if (n == null || n == 0) {
            jdbc.update("INSERT INTO admin_user (username, password_hash) VALUES (?,?)", defaultUser, encoder.encode(defaultPass));
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        List<String> l = jdbc.queryForList("SELECT password_hash FROM admin_user WHERE username=?", String.class, username);
        if (l.isEmpty()) throw new UsernameNotFoundException("not found");
        return User.withUsername(username).password(l.get(0)).roles("ADMIN").build();
    }

    public boolean changePassword(String username, String current, String next) {
        List<String> l = jdbc.queryForList("SELECT password_hash FROM admin_user WHERE username=?", String.class, username);
        if (l.isEmpty() || !encoder.matches(current, l.get(0))) return false;
        jdbc.update("UPDATE admin_user SET password_hash=?, updated_at=CURRENT_TIMESTAMP WHERE username=?", encoder.encode(next), username);
        return true;
    }
}
