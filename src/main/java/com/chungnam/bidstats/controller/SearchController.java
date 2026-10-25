package com.chungnam.bidstats.controller;

import com.chungnam.bidstats.dto.SearchFormDto;
import com.chungnam.bidstats.service.BidSearchService;
import com.chungnam.bidstats.service.SettingsService;
import com.chungnam.bidstats.vo.SearchPresetVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {
    private final BidSearchService search; private final SettingsService settings;
    public SearchController(BidSearchService b, SettingsService s) { search = b; settings = s; }

    @GetMapping("/search")
    public String search(@ModelAttribute("form") SearchFormDto form,
                         @RequestParam(required = false) Long presetId,
                         @RequestParam(required = false) String run,
                         @RequestParam(defaultValue = "1") int page, Model m) {
        if (presetId != null) {
            SearchPresetVo p = settings.get(presetId);
            if (p != null) {
                form.setRegionCode(p.getRegionCode());
                if (p.getDateFrom() != null) form.setDateFrom(p.getDateFrom());
                if (p.getDateTo() != null) form.setDateTo(p.getDateTo());
                form.setMinPrice(p.getMinPrice()); form.setMaxPrice(p.getMaxPrice()); form.setKeyword(p.getKeyword());
                // 프리셋은 폼만 채움. 나라장터 호출은 검색 버튼(run=1)에서.
            }
        }
        m.addAttribute("regions", settings.regions());
        m.addAttribute("presets", settings.list());
        m.addAttribute("mockMode", search.isMockMode());
        if (run != null) {
            try {
                com.chungnam.bidstats.util.PageResult.apply(m, search.search(form), page, form.toQuery() + "&run=1");
                m.addAttribute("statsQuery", form.toQuery());
            } catch (Exception ex) {
                m.addAttribute("errorMessage", ex.getMessage());
                com.chungnam.bidstats.util.PageResult.apply(m, java.util.List.of(), 1, "");
            }
        }
        return "search";
    }
}
