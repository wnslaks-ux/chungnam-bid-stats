package com.chungnam.bidstats.service;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** 운영 설정(app_config 테이블). 재시작 후에도 유지. */
@Service
public class AppConfigService {
    private final JdbcTemplate jdbc;
    public AppConfigService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public String get(String key) {
        List<String> l = jdbc.queryForList("SELECT config_value FROM app_config WHERE config_key=?", String.class, key);
        return l.isEmpty() ? null : l.get(0);
    }
    public void set(String key, String value) {
        jdbc.update("MERGE INTO app_config (config_key, config_value, updated_at) KEY(config_key) VALUES (?,?,CURRENT_TIMESTAMP)", key, value);
    }
    public void remove(String key) { jdbc.update("DELETE FROM app_config WHERE config_key=?", key); }
}
