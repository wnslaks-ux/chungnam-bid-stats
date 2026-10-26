package com.chungnam.bidstats.service;

import com.chungnam.bidstats.config.Regions;
import com.chungnam.bidstats.dto.SearchFormDto;
import com.chungnam.bidstats.mapper.BidCacheMapper;
import com.chungnam.bidstats.vo.BidNoticeVo;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class BidSearchService {
    private final G2bApiClient client;
    private final BidCacheMapper cacheMapper;

    public BidSearchService(G2bApiClient client, BidCacheMapper cacheMapper) {
        this.client = client; this.cacheMapper = cacheMapper;
    }

    public boolean isMockMode() { return client.isMockMode(); }

    private static final Logger log = LoggerFactory.getLogger(BidSearchService.class);
    private static final String KEY_FP = "bidCache.fp", KEY_LIST = "bidCache.list";

    /** 검색 폼 지문(지역+기간+필터). 같으면 세션 캐시 재사용 → 페이지 이동 시 외부 API 재호출 없음 */
    private String fingerprint(SearchFormDto f) {
        return String.join("|", String.valueOf(f.getRegionCode()), String.valueOf(f.getDateFrom()),
            String.valueOf(f.getDateTo()), String.valueOf(f.getKeyword() == null ? "" : f.getKeyword().trim()),
            String.valueOf(f.getMinPrice()), String.valueOf(f.getMaxPrice()), String.valueOf(isMockMode()));
    }

    /** 세션 간 공유 메모리 캐시(10분 TTL) — 다른 사용자/새 세션도 같은 조건이면 즉시 */
    private static final long TTL_MS = 10 * 60 * 1000L;
    private record Entry(long at, List<BidNoticeVo> list) { }
    private final java.util.concurrent.ConcurrentHashMap<String, Entry> shared = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.ExecutorService DB_POOL = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "bid-cache-db"); t.setDaemon(true); return t;
    });

    @SuppressWarnings("unchecked")
    public List<BidNoticeVo> search(SearchFormDto f) {
        String fp = fingerprint(f);
        HttpSession session = null;
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) session = sra.getRequest().getSession(true);
        if (session != null && fp.equals(session.getAttribute(KEY_FP))) {
            Object cached = session.getAttribute(KEY_LIST);
            if (cached instanceof List<?> l) {
                log.info("bid search cache HIT fp={} size={}", fp, l.size());
                return (List<BidNoticeVo>) l;
            }
        }
        long now = System.currentTimeMillis();
        shared.values().removeIf(e -> now - e.at() > TTL_MS);
        Entry e = shared.get(fp);
        List<BidNoticeVo> list;
        if (e != null) {
            list = e.list();
            log.info("bid search shared cache HIT fp={} size={}", fp, list.size());
        } else {
            long t0 = System.currentTimeMillis();
            G2bApiClient.FetchResult r = fetch(f);
            list = r.list();
            if (r.partial()) {
                // 일부 구간 실패: 캐시하지 않고(다음 검색 때 재시도) 경고 표시
                if (attrs instanceof ServletRequestAttributes sra2) sra2.getRequest().setAttribute("g2bWarning",
                    "나라장터 응답 지연으로 일부 기간(" + String.join(", ", r.failedRanges()) + ")이 누락되었습니다. 다시 검색하면 보완됩니다.");
                log.info("bid search PARTIAL fp={} failed={}", fp, r.failedRanges());
                return list;
            }
            shared.put(fp, new Entry(System.currentTimeMillis(), list));
            log.info("bid search cache MISS fp={} size={} fetched in {}ms", fp, list.size(), System.currentTimeMillis() - t0);
        }
        if (session != null) {
            session.setAttribute(KEY_LIST, list);
            session.setAttribute(KEY_FP, fp);
        }
        return list;
    }

    private G2bApiClient.FetchResult fetch(SearchFormDto f) {
        String regionName = Regions.nameOf(f.getRegionCode());
        G2bApiClient.FetchResult raw = client.fetchConstructionBidsResult(regionName, f);
        List<BidNoticeVo> list = raw.list().stream()
            .filter(v -> f.getMinPrice() == null || v.getPresmptPrce() >= f.getMinPrice())
            .filter(v -> f.getMaxPrice() == null || v.getPresmptPrce() <= f.getMaxPrice())
            .filter(v -> f.getKeyword() == null || f.getKeyword().isBlank() || v.getBidNtceNm().contains(f.getKeyword()))
            .collect(Collectors.toCollection(ArrayList::new));
        if (!client.isMockMode()) {
            List<BidNoticeVo> copy = List.copyOf(list);
            DB_POOL.submit(() -> { try { copy.forEach(cacheMapper::merge); } catch (Exception ex) { log.warn("bid_cache merge 실패: {}", ex.getMessage()); } });
        }
        return new G2bApiClient.FetchResult(java.util.Collections.unmodifiableList(list), raw.failedRanges());
    }
}
