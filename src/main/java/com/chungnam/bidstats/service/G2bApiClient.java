package com.chungnam.bidstats.service;

import com.chungnam.bidstats.dto.SearchFormDto;
import com.chungnam.bidstats.vo.BidNoticeVo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.LinkedHashMap;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/** 나라장터 입찰공고정보서비스(공사) 클라이언트. 서비스키가 없으면 Mock 데이터 반환. */
@Component
public class G2bApiClient {
    private static final Logger log = LoggerFactory.getLogger(G2bApiClient.class);
    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 월 구간 병렬 조회용 소규모 데몬 스레드 풀 */
    private static final ExecutorService POOL = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "g2b-fetch"); t.setDaemon(true); return t;
    });

    private final RestTemplate restTemplate;
    private final ObjectMapper om = new ObjectMapper();
    @Value("${g2b.base-url}") private String baseUrl;
    @Value("${g2b.service-key:}") private String serviceKey;
    @Value("${g2b.num-of-rows:100}") private int numOfRows;

    public static final String CONFIG_KEY = "g2b.service-key";
    private final AppConfigService config;

    public G2bApiClient(RestTemplate restTemplate, AppConfigService config) { this.restTemplate = restTemplate; this.config = config; }

    /** DB(app_config) 값 우선, 없으면 application.yml 값 */
    private String key() {
        try {
            String v = config.get(CONFIG_KEY);
            if (v != null && !v.isBlank()) return v.trim();
        } catch (Exception ignore) { }
        return serviceKey == null ? "" : serviceKey.trim();
    }

    public String keyStatus() {
        try { String v = config.get(CONFIG_KEY); if (v != null && !v.isBlank()) return "DB"; } catch (Exception ignore) { }
        return (serviceKey == null || serviceKey.isBlank()) ? "NONE" : "YML";
    }

    public String maskedKey() {
        String k = key();
        if (k.isEmpty()) return "";
        if (k.length() <= 8) return "****";
        return k.substring(0, 4) + "****" + k.substring(k.length() - 4);
    }

    public boolean isMockMode() {
        String k = key();
        return k.isBlank() || k.contains("YOUR") || k.contains("\uBC1C\uAE09");
    }

    public List<BidNoticeVo> fetchConstructionBids(String regionName, SearchFormDto f) {
        if (isMockMode()) return mock(regionName, f);
        // 나라장터는 조회기간을 한 번에 약 1개월만 허용 → 월 단위로 나눠 합침
        Map<String, BidNoticeVo> dedup = new LinkedHashMap<>();
        LocalDate cursor = f.getDateFrom();
        LocalDate end = f.getDateTo();
        if (cursor == null || end == null || cursor.isAfter(end)) {
            throw new IllegalStateException("조회 기간이 올바르지 않습니다.");
        }
        // 월 구간 목록 생성 (최대 24개월)
        List<LocalDate[]> ranges = new ArrayList<>();
        while (!cursor.isAfter(end) && ranges.size() < 24) {
            LocalDate windowEnd = cursor.plusMonths(1).minusDays(1);
            if (windowEnd.isAfter(end)) windowEnd = end;
            ranges.add(new LocalDate[]{cursor, windowEnd});
            cursor = windowEnd.plusDays(1);
        }
        // 월 구간을 병렬 조회(소규모 풀), 결과는 기간 순서대로 병합+공고번호 중복 제거
        List<CompletableFuture<List<BidNoticeVo>>> futures = new ArrayList<>();
        for (LocalDate[] r : ranges) {
            futures.add(CompletableFuture.supplyAsync(() -> fetchWindow(regionName, f, r[0], r[1]), POOL));
        }
        try {
            for (CompletableFuture<List<BidNoticeVo>> fu : futures) {
                for (BidNoticeVo v : fu.join()) dedup.putIfAbsent(v.getBidNtceNo(), v);
            }
        } catch (CompletionException e) {
            futures.forEach(x -> x.cancel(true));
            Throwable c = e.getCause();
            if (c instanceof IllegalStateException ise) throw ise;
            throw new IllegalStateException("나라장터 API 호출 실패: " + (c == null ? e.getMessage() : c.getMessage()), c);
        }
        return new ArrayList<>(dedup.values());
    }

    private List<BidNoticeVo> fetchWindow(String regionName, SearchFormDto f, LocalDate from, LocalDate to) {
        List<BidNoticeVo> all = new ArrayList<>();
        int page = 1;
        while (page <= 10) {
            URI uri = buildUri(regionName, f, from, to, page);
            try {
                String body = restTemplate.getForObject(uri, String.class);
                if (body == null || body.isBlank()) break;
                JsonNode response = om.readTree(body).path("response");
                String code = response.path("header").path("resultCode").asText("");
                String msg = response.path("header").path("resultMsg").asText("");
                if (!code.isEmpty() && !"00".equals(code) && !"0".equals(code)) {
                    log.error("나라장터 API 오류 resultCode={} resultMsg={} ({}~{})", code, msg, from, to);
                    throw new IllegalStateException("나라장터 API 오류: " + code + " " + msg);
                }
                JsonNode root = response.path("body");
                JsonNode items = root.path("items");
                if (items.isMissingNode() || items.isNull()) break;
                if (items.isObject() && items.has("item")) items = items.get("item");
                if (items.isObject()) {
                    all.add(map(items));
                    break;
                }
                if (!items.isArray() || items.size() == 0) break;
                for (JsonNode it : items) all.add(map(it));
                int total = root.path("totalCount").asInt(0);
                if (page * numOfRows >= total) break;
                page++;
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                log.error("나라장터 API 호출 실패: {}", e.getMessage());
                throw new IllegalStateException("나라장터 API 호출 실패: " + e.getMessage(), e);
            }
        }
        return all;
    }

    private URI buildUri(String regionName, SearchFormDto f, LocalDate from, LocalDate to, int page) {
        String key = key();
        String keyParam = key.contains("%") ? key : enc(key);
        StringBuilder sb = new StringBuilder();
        sb.append(baseUrl).append("/getBidPblancListInfoCnstwkPPSSrch");
        sb.append("?serviceKey=").append(keyParam);
        sb.append("&type=json");
        sb.append("&inqryDiv=1");
        sb.append("&inqryBgnDt=").append(from.format(YMD)).append("0000");
        sb.append("&inqryEndDt=").append(to.format(YMD)).append("2359");
        sb.append("&dminsttNm=").append(enc(regionName));
        sb.append("&pageNo=").append(page);
        sb.append("&numOfRows=").append(numOfRows);
        if (f.getKeyword() != null && !f.getKeyword().isBlank()) {
            sb.append("&bidNtceNm=").append(enc(f.getKeyword().trim()));
        }
        return URI.create(sb.toString());
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private BidNoticeVo map(JsonNode it) {
        BidNoticeVo v = new BidNoticeVo();
        v.setBidNtceNo(it.path("bidNtceNo").asText() + "-" + it.path("bidNtceOrd").asText("000"));
        v.setBidNtceNm(it.path("bidNtceNm").asText());
        v.setDminsttNm(it.path("dminsttNm").asText());
        v.setBidNtceDt(it.path("bidNtceDt").asText());
        String p = it.path("presmptPrce").asText("").replaceAll("[^0-9]", "");
        v.setPresmptPrce(p.isEmpty() ? 0L : Long.parseLong(p));
        v.setBidNtceUrl(it.path("bidNtceDtlUrl").asText(it.path("bidNtceUrl").asText()));
        return v;
    }

    private List<BidNoticeVo> mock(String regionName, SearchFormDto f) {
        String[] works = {
            "\uB3C4\uB85C \uD3EC\uC7A5\uACF5\uC0AC",
            "\uB18D\uBC30\uC218\uB85C \uC815\uBE44\uACF5\uC0AC",
            "\uD558\uCC9C \uC815\uBE44\uACF5\uC0AC",
            "\uB9C8\uC744\uD68C\uAD00 \uC2E0\uCD95\uACF5\uC0AC",
            "\uC0C1\uC218\uB3C4 \uAD00\uB85C \uACF5\uC0AC",
            "\uC18C\uADDC\uBAA8 \uC8FC\uBBFC\uC219\uC6D0\uC0AC\uC5C5",
            "\uAD50\uB7C9 \uBCF4\uC218\uBCF4\uAC15\uACF5\uC0AC",
            "\uCCB4\uC721\uC2DC\uC124 \uC870\uC131\uACF5\uC0AC",
            "\uBC30\uC218\uC9C0 \uAC1C\uB7C9\uACF5\uC0AC",
            "\uACBD\uB85C\uB2F9 \uB9AC\uBAA8\uB378\uB9C1"
        };
        Random r = new Random(regionName.hashCode());
        List<BidNoticeVo> list = new ArrayList<>();
        LocalDate from = f.getDateFrom(), to = f.getDateTo();
        long days = Math.max(1, to.toEpochDay() - from.toEpochDay());
        for (int i = 0; i < 40; i++) {
            LocalDate d = from.plusDays((long) (r.nextDouble() * days));
            BidNoticeVo v = new BidNoticeVo();
            v.setBidNtceNo("MOCK" + d.format(YMD) + String.format("%03d", i) + "-000");
            v.setBidNtceNm(regionName + " " + works[r.nextInt(works.length)] + " (" + (i + 1) + ")");
            v.setDminsttNm("\uCDA9\uCCAD\uB0A8\uB3C4 " + regionName);
            v.setBidNtceDt(d + " 10:00:00");
            v.setPresmptPrce((long) (20_000_000 + r.nextInt(980) * 1_000_000L));
            v.setBidNtceUrl("https://www.g2b.go.kr");
            list.add(v);
        }
        list.sort((a, b) -> b.getBidNtceDt().compareTo(a.getBidNtceDt()));
        return list;
    }
}
