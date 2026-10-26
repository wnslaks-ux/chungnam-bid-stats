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
    private static final ExecutorService POOL = Executors.newFixedThreadPool(8, r -> {
        Thread t = new Thread(r, "g2b-fetch"); t.setDaemon(true); return t;
    });
    /** 같은 월 구간 안의 2페이지 이후를 병렬 조회 (월 풀과 분리해 교착 방지) */
    private static final ExecutorService PAGE_POOL = Executors.newFixedThreadPool(16, r -> {
        Thread t = new Thread(r, "g2b-page"); t.setDaemon(true); return t;
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

    /** 조회 결과 + 실패한 구간 목록(부분 실패 허용) */
    public record FetchResult(List<BidNoticeVo> list, List<String> failedRanges) {
        public boolean partial() { return !failedRanges.isEmpty(); }
    }

    /** 동시에 나라장터로 나가는 HTTP 요청 수 상한(서버 과부하·타임아웃 방지) */
    private static final java.util.concurrent.Semaphore HTTP_SLOTS = new java.util.concurrent.Semaphore(10);
    private static final int CHUNK_DAYS = 15;
    private static final int MAX_RETRIES = 2;

    public List<BidNoticeVo> fetchConstructionBids(String regionName, SearchFormDto f) {
        return fetchConstructionBidsResult(regionName, f).list();
    }

    public FetchResult fetchConstructionBidsResult(String regionName, SearchFormDto f) {
        if (isMockMode()) return new FetchResult(mock(regionName, f), List.of());
        Map<String, BidNoticeVo> dedup = new LinkedHashMap<>();
        LocalDate cursor = f.getDateFrom();
        LocalDate end = f.getDateTo();
        if (cursor == null || end == null || cursor.isAfter(end)) {
            throw new IllegalStateException("조회 기간이 올바르지 않습니다.");
        }
        // 긴 기간은 15일 단위로 잘게 나눠 응답 지연(타임아웃) 위험을 줄임 (최대 약 2년)
        List<LocalDate[]> ranges = new ArrayList<>();
        while (!cursor.isAfter(end) && ranges.size() < 50) {
            // 달력 기준(1~15일, 16일~말일)으로 고정 분할 → 다른 기간 검색과도 구간 캐시 공유
            LocalDate windowEnd = cursor.getDayOfMonth() <= 15 ? cursor.withDayOfMonth(15) : cursor.withDayOfMonth(cursor.lengthOfMonth());
            if (windowEnd.isAfter(end)) windowEnd = end;
            ranges.add(new LocalDate[]{cursor, windowEnd});
            cursor = windowEnd.plusDays(1);
        }
        List<CompletableFuture<List<BidNoticeVo>>> futures = new ArrayList<>();
        for (LocalDate[] r : ranges) {
            futures.add(CompletableFuture.supplyAsync(() -> cachedWindow(regionName, f, r[0], r[1]), POOL));
        }
        List<String> failed = new ArrayList<>();
        Throwable lastErr = null;
        for (int i = 0; i < futures.size(); i++) {
            try {
                for (BidNoticeVo v : futures.get(i).join()) {
                    // 같은 공고의 정정·재공고(차수)는 1건으로: 최신 차수만 유지
                    String base = v.getBidNtceNo().replaceAll("-[^-]*$", "");
                    BidNoticeVo prev = dedup.get(base);
                    if (prev == null || prev.getBidNtceNo().compareTo(v.getBidNtceNo()) < 0) dedup.put(base, v);
                }
            } catch (CompletionException e) {
                Throwable c = e.getCause() == null ? e : e.getCause();
                // 인증키 오류 등 API 자체 오류는 전체 실패로 처리
                if (c instanceof IllegalStateException ise && ise.getMessage() != null && ise.getMessage().startsWith("나라장터 API 오류")) {
                    futures.forEach(x -> x.cancel(true));
                    throw ise;
                }
                lastErr = c;
                failed.add(ranges.get(i)[0] + "~" + ranges.get(i)[1]);
                log.warn("구간 조회 실패 {}~{}: {}", ranges.get(i)[0], ranges.get(i)[1], c.getMessage());
            }
        }
        if (!failed.isEmpty() && failed.size() == ranges.size()) {
            throw new IllegalStateException(friendly(lastErr), lastErr);
        }
        return new FetchResult(new ArrayList<>(dedup.values()), failed);
    }

    private static boolean isTimeout(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c instanceof java.net.SocketTimeoutException) return true;
            if (c.getMessage() != null && c.getMessage().contains("timed out")) return true;
        }
        return false;
    }

    static String friendly(Throwable t) {
        if (isTimeout(t)) return "나라장터 서버 응답이 지연되어 조회하지 못했습니다. 잠시 후 다시 검색하거나 기간을 줄여 주세요.";
        return "나라장터 API 호출 실패: " + (t == null ? "" : t.getMessage());
    }

    /** 구간 단위 캐시: 이미 지난 구간(어제 이전)은 12시간, 최근 구간은 10분 재사용.
     *  1년 검색 후 6개월/3개월 검색, 페이지 이동·통계 등은 겹치는 구간을 다시 부르지 않음. */
    private record WinEntry(long at, long ttl, List<BidNoticeVo> list) { }
    private static final java.util.concurrent.ConcurrentHashMap<String, WinEntry> WIN_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long PAST_TTL = 12 * 3600_000L, RECENT_TTL = 10 * 60_000L;

    private List<BidNoticeVo> cachedWindow(String regionName, SearchFormDto f, LocalDate from, LocalDate to) {
        String kw = f.getKeyword() == null ? "" : f.getKeyword().trim();
        String k = regionName + "|" + kw + "|" + from + "|" + to;
        long now = System.currentTimeMillis();
        WinEntry e = WIN_CACHE.get(k);
        if (e != null && now - e.at() < e.ttl()) return e.list();
        List<BidNoticeVo> list = List.copyOf(fetchWindow(regionName, f, from, to));
        long ttl = to.isBefore(LocalDate.now().minusDays(1)) ? PAST_TTL : RECENT_TTL;
        if (WIN_CACHE.size() > 2000) WIN_CACHE.entrySet().removeIf(x -> now - x.getValue().at() > x.getValue().ttl());
        WIN_CACHE.put(k, new WinEntry(now, ttl, list));
        return list;
    }

    private List<BidNoticeVo> fetchWindow(String regionName, SearchFormDto f, LocalDate from, LocalDate to) {
        List<BidNoticeVo> all = new ArrayList<>();
        int total = fetchPageRetry(regionName, f, from, to, 1, all);
        int need = (int) Math.ceil(total / (double) numOfRows);
        int pages = Math.min(20, need);
        if (need > pages) log.warn("구간 {}~{} 결과 {}건 중 일부만 조회(페이지 상한)", from, to, total);
        if (pages <= 1) return all;
        List<CompletableFuture<List<BidNoticeVo>>> rest = new ArrayList<>();
        for (int p = 2; p <= pages; p++) {
            final int pg = p;
            rest.add(CompletableFuture.supplyAsync(() -> {
                List<BidNoticeVo> out = new ArrayList<>();
                fetchPageRetry(regionName, f, from, to, pg, out);
                return out;
            }, PAGE_POOL));
        }
        try {
            for (CompletableFuture<List<BidNoticeVo>> fu : rest) all.addAll(fu.join());
        } catch (CompletionException e) {
            rest.forEach(x -> x.cancel(true));
            Throwable c = e.getCause();
            if (c instanceof IllegalStateException ise) throw ise;
            throw new IllegalStateException("나라장터 API 호출 실패: " + (c == null ? e.getMessage() : c.getMessage()), c);
        }
        return all;
    }

    /** 타임아웃/일시 오류 시 최대 2회 재시도(1.5s, 3s 백오프). 동시 요청 수는 세마포어로 제한 */
    private int fetchPageRetry(String regionName, SearchFormDto f, LocalDate from, LocalDate to, int page, List<BidNoticeVo> out) {
        for (int attempt = 0; ; attempt++) {
            List<BidNoticeVo> tmp = new ArrayList<>();
            try {
                HTTP_SLOTS.acquire();
                int total;
                try { total = fetchPage(regionName, f, from, to, page, tmp); }
                finally { HTTP_SLOTS.release(); }
                out.addAll(tmp);
                return total;
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("나라장터 조회가 중단되었습니다.", ie);
            } catch (IllegalStateException e) {
                boolean apiErr = e.getMessage() != null && e.getMessage().startsWith("나라장터 API 오류");
                if (apiErr || attempt >= MAX_RETRIES) throw e;
                log.warn("g2b 재시도 {}/{} page {} {}~{} ({})", attempt + 1, MAX_RETRIES, page, from, to, e.getMessage());
                try { Thread.sleep(1500L * (attempt + 1)); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw e; }
            }
        }
    }

    /** 한 페이지 조회 → out에 추가, totalCount 반환 */
    private int fetchPage(String regionName, SearchFormDto f, LocalDate from, LocalDate to, int page, List<BidNoticeVo> out) {
        URI uri = buildUri(regionName, f, from, to, page);
        long t0 = System.currentTimeMillis();
        try {
            String body = restTemplate.getForObject(uri, String.class);
            if (body == null || body.isBlank()) return 0;
            JsonNode response = om.readTree(body).path("response");
            String code = response.path("header").path("resultCode").asText("");
            String msg = response.path("header").path("resultMsg").asText("");
            if (!code.isEmpty() && !"00".equals(code) && !"0".equals(code)) {
                log.error("나라장터 API 오류 resultCode={} resultMsg={} ({}~{})", code, msg, from, to);
                throw new IllegalStateException("나라장터 API 오류: " + code + " " + msg);
            }
            JsonNode root = response.path("body");
            int total = root.path("totalCount").asInt(0);
            JsonNode items = root.path("items");
            if (items.isMissingNode() || items.isNull()) return total;
            if (items.isObject() && items.has("item")) items = items.get("item");
            if (items.isObject()) { out.add(map(items)); return Math.max(total, 1); }
            if (items.isArray()) for (JsonNode it : items) out.add(map(it));
            return total;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("나라장터 API 호출 실패: {}", e.getMessage());
            throw new IllegalStateException("나라장터 API 호출 실패: " + e.getMessage(), e);
        } finally {
            log.debug("g2b page {} {}~{} {}ms", page, from, to, System.currentTimeMillis() - t0);
        }
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
