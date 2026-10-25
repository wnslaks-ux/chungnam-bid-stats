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
        long t0 = System.currentTimeMillis();
        List<BidNoticeVo> list = fetch(f);
        log.info("bid search cache MISS fp={} size={} fetched in {}ms", fp, list.size(), System.currentTimeMillis() - t0);
        if (session != null) {
            session.setAttribute(KEY_LIST, list);
            session.setAttribute(KEY_FP, fp);
        }
        return list;
    }

    private List<BidNoticeVo> fetch(SearchFormDto f) {
        String regionName = Regions.nameOf(f.getRegionCode());
        List<BidNoticeVo> list = client.fetchConstructionBids(regionName, f).stream()
            .filter(v -> f.getMinPrice() == null || v.getPresmptPrce() >= f.getMinPrice())
            .filter(v -> f.getMaxPrice() == null || v.getPresmptPrce() <= f.getMaxPrice())
            .filter(v -> f.getKeyword() == null || f.getKeyword().isBlank() || v.getBidNtceNm().contains(f.getKeyword()))
            .collect(Collectors.toCollection(ArrayList::new));
        if (!client.isMockMode()) list.forEach(cacheMapper::merge);
        return java.util.Collections.unmodifiableList(list);
    }
}
