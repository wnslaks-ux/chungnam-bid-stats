package com.chungnam.bidstats.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 기동 직후 검색/통계/설정 화면을 한 번씩 열어 JSP를 미리 컴파일한다.
 * (첫 클릭 때 컴파일하느라 느려지는 현상 방지. 나라장터 API는 호출하지 않음.)
 */
@Component
public class PageWarmup implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(PageWarmup.class);
    private final RestTemplate restTemplate;

    public PageWarmup(RestTemplate restTemplate) { this.restTemplate = restTemplate; }

    @Override
    public void run(ApplicationArguments args) {
        String port = System.getenv().getOrDefault("PORT", "8080");
        String base = "http://127.0.0.1:" + port;
        for (String path : new String[]{"/", "/search", "/stats", "/settings", "/stats?regionCode=YESAN"}) {
            try {
                restTemplate.getForEntity(base + path, String.class);
                log.info("page warmup ok {}", path);
            } catch (Exception e) {
                log.warn("page warmup skip {}: {}", path, e.getMessage());
            }
        }
    }
}
