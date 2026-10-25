package com.chungnam.bidstats.service;

import com.chungnam.bidstats.config.Regions;
import com.chungnam.bidstats.dto.SearchFormDto;
import com.chungnam.bidstats.mapper.BidCacheMapper;
import com.chungnam.bidstats.vo.BidNoticeVo;
import java.util.List;
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

    public List<BidNoticeVo> search(SearchFormDto f) {
        String regionName = Regions.nameOf(f.getRegionCode());
        List<BidNoticeVo> list = client.fetchConstructionBids(regionName, f).stream()
            .filter(v -> f.getMinPrice() == null || v.getPresmptPrce() >= f.getMinPrice())
            .filter(v -> f.getMaxPrice() == null || v.getPresmptPrce() <= f.getMaxPrice())
            .filter(v -> f.getKeyword() == null || f.getKeyword().isBlank() || v.getBidNtceNm().contains(f.getKeyword()))
            .collect(Collectors.toList());
        if (!client.isMockMode()) list.forEach(cacheMapper::merge);
        return list;
    }
}
