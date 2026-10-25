package com.chungnam.bidstats.controller;

import com.chungnam.bidstats.service.SettingsService;
import com.chungnam.bidstats.vo.RegionVo;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class RegionController {
    private final SettingsService settings;
    public RegionController(SettingsService s) { settings = s; }

    /** 지역 목록 JSON */
    @GetMapping("/api/regions")
    public List<RegionVo> regions() { return settings.regions(); }

    /** 지역 카드 클릭 → 해당 지역 통계로 이동 */
    @GetMapping("/region/{code}")
    public RedirectView go(@PathVariable String code) { return new RedirectView("/stats?regionCode=" + code); }
}
