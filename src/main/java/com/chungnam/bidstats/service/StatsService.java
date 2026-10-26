package com.chungnam.bidstats.service;

import com.chungnam.bidstats.vo.BidNoticeVo;
import com.chungnam.bidstats.vo.StatsSummaryVo;
import java.util.List;
import java.util.TreeMap;
import org.springframework.stereotype.Service;

@Service
public class StatsService {
    public StatsSummaryVo summarize(String regionName, List<BidNoticeVo> list) { return summarize(regionName, list, null, null); }

    /** 기간이 주어지면 공고가 없는 달도 0으로 채워 차트 축이 끊기지 않게 함 */
    public StatsSummaryVo summarize(String regionName, List<BidNoticeVo> list, java.time.LocalDate from, java.time.LocalDate to) {
        StatsSummaryVo s = new StatsSummaryVo();
        s.setRegionName(regionName);
        s.setCount(list.size());
        long total = 0, max = 0, min = list.isEmpty() ? 0 : Long.MAX_VALUE;
        TreeMap<String, Long> mt = new TreeMap<>();
        TreeMap<String, Integer> mc = new TreeMap<>();
        if (from != null && to != null && !from.isAfter(to)) {
            for (java.time.YearMonth ym = java.time.YearMonth.from(from); !ym.isAfter(java.time.YearMonth.from(to)); ym = ym.plusMonths(1)) {
                mt.put(ym.toString(), 0L); mc.put(ym.toString(), 0);
            }
        }
        for (BidNoticeVo v : list) {
            long p = v.getPresmptPrce() == null ? 0 : v.getPresmptPrce();
            total += p; max = Math.max(max, p); min = Math.min(min, p);
            String dt = v.getBidNtceDt() == null ? "" : v.getBidNtceDt();
            String ym = dt.length() >= 7 ? dt.substring(0, 7).replace('/', '-') : "미상";
            mt.merge(ym, p, Long::sum);
            mc.merge(ym, 1, Integer::sum);
        }
        s.setTotalPrice(total); s.setMaxPrice(max); s.setMinPrice(min);
        s.setAvgPrice(list.isEmpty() ? 0 : total / list.size());
        s.setMonthlyTotal(mt); s.setMonthlyCount(mc);
        return s;
    }
}
