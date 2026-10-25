package com.chungnam.bidstats.controller;

import com.chungnam.bidstats.service.BidSearchService;
import com.chungnam.bidstats.service.SettingsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final SettingsService settings; private final BidSearchService search;
    public HomeController(SettingsService s, BidSearchService b) { settings = s; search = b; }

    @GetMapping("/")
    public String home(Model m) {
        m.addAttribute("regions", settings.regions());
        m.addAttribute("mockMode", search.isMockMode());
        return "home";
    }
}
