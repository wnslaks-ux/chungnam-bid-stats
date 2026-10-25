package com.chungnam.bidstats.controller;

import com.chungnam.bidstats.config.Regions;
import com.chungnam.bidstats.dto.SearchFormDto;
import com.chungnam.bidstats.service.BidSearchService;
import com.chungnam.bidstats.service.SettingsService;
import com.chungnam.bidstats.service.StatsService;
import com.chungnam.bidstats.vo.BidNoticeVo;
import com.chungnam.bidstats.vo.StatsSummaryVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import com.chungnam.bidstats.util.PageResult;

@Controller
public class StatsController {
    private final BidSearchService search; private final StatsService stats; private final SettingsService settings;
    private final ObjectMapper om = new ObjectMapper();
    public StatsController(BidSearchService b, StatsService st, SettingsService s) { search = b; stats = st; settings = s; }

    @GetMapping("/stats")
    public String stats(@ModelAttribute("form") SearchFormDto form,
                        @RequestParam(defaultValue = "1") int page, Model m) throws Exception {
        m.addAttribute("regions", settings.regions());
        m.addAttribute("mockMode", search.isMockMode());
        try {
            List<BidNoticeVo> list = search.search(form);
            StatsSummaryVo s = stats.summarize(Regions.nameOf(form.getRegionCode()), list);
            m.addAttribute("summary", s);
            PageResult.apply(m, list, page, form.toQuery());
            m.addAttribute("chartLabels", om.writeValueAsString(s.getMonthlyTotal().keySet()));
            m.addAttribute("chartTotals", om.writeValueAsString(s.getMonthlyTotal().values()));
            m.addAttribute("chartCounts", om.writeValueAsString(s.getMonthlyCount().values()));
        } catch (Exception ex) {
            m.addAttribute("errorMessage", ex.getMessage());
            StatsSummaryVo empty = stats.summarize(Regions.nameOf(form.getRegionCode()), List.of());
            m.addAttribute("summary", empty);
            PageResult.apply(m, List.of(), 1, "");
            m.addAttribute("chartLabels", "[]");
            m.addAttribute("chartTotals", "[]");
            m.addAttribute("chartCounts", "[]");
        }
        return "stats";
    }
}
