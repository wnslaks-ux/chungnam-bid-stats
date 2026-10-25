package com.chungnam.bidstats.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** 운영 설정(app_config 테이블). 읽기는 메모리 캐시로 페이지 전환을 빠르게. */
@Service
public class AppConfigService {
    private final JdbcTemplate jdbc;
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    private static final String NULL_MARK = "\0";

    public AppConfigService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public String get(String key) {
        String hit = cache.get(key);
        if (hit != null) return NULL_MARK.equals(hit) ? null : hit;
        List<String> l = jdbc.queryForList("SELECT config_value FROM app_config WHERE config_key=?", String.class, key);
        String v = l.isEmpty() ? null : l.get(0);
        cache.put(key, v == null ? NULL_MARK : v);
        return v;
    }

    public void set(String key, String value) {
        jdbc.update("MERGE INTO app_config (config_key, config_value, updated_at) KEY(config_key) VALUES (?,?,CURRENT_TIMESTAMP)", key, value);
        cache.put(key, value == null ? NULL_MARK : value);
    }

    public void remove(String key) {
        jdbc.update("DELETE FROM app_config WHERE config_key=?", key);
        cache.put(key, NULL_MARK);
    }
}
