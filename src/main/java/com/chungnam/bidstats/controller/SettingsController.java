package com.chungnam.bidstats.controller;

import com.chungnam.bidstats.service.BidSearchService;
import com.chungnam.bidstats.service.SettingsService;
import com.chungnam.bidstats.vo.SearchPresetVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/settings")
public class SettingsController {
    private final SettingsService settings;
    private final BidSearchService search;

    public SettingsController(SettingsService s, BidSearchService search) {
        this.settings = s; this.search = search;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long editId, Model m) {
        m.addAttribute("presets", settings.list());
        m.addAttribute("regions", settings.regions());
        m.addAttribute("mockMode", search.isMockMode());
        SearchPresetVo edit = editId != null ? settings.get(editId) : null;
        m.addAttribute("edit", edit != null ? edit : new SearchPresetVo());
        return "settings";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute SearchPresetVo vo) {
        settings.save(vo);
        return "redirect:/settings";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        settings.delete(id);
        return "redirect:/settings";
    }
}
