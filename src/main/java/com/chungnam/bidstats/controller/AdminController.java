package com.chungnam.bidstats.controller;

import com.chungnam.bidstats.service.AppConfigService;
import com.chungnam.bidstats.service.AdminUserService;
import com.chungnam.bidstats.service.BidSearchService;
import com.chungnam.bidstats.service.G2bApiClient;
import com.chungnam.bidstats.service.SettingsService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {
    private final SettingsService settings; private final BidSearchService search;
    private final AppConfigService config; private final AdminUserService users; private final G2bApiClient g2b;
    public AdminController(SettingsService s, BidSearchService b, AppConfigService c, AdminUserService u, G2bApiClient g) {
        settings = s; search = b; config = c; users = u; g2b = g;
    }

    @ModelAttribute
    public void common(Model m) {
        m.addAttribute("regions", settings.regions());
        m.addAttribute("mockMode", search.isMockMode());
    }

    @GetMapping("/login") public String login() { return "login"; }

    @GetMapping("/admin")
    public String dashboard(Model m) {
        m.addAttribute("keyStatus", g2b.keyStatus());
        m.addAttribute("keyMasked", g2b.maskedKey());
        return "admin/dashboard";
    }

    @GetMapping("/admin/api-key")
    public String apiKey(Model m) {
        m.addAttribute("keyStatus", g2b.keyStatus());
        m.addAttribute("keyMasked", g2b.maskedKey());
        return "admin/apiKey";
    }

    @PostMapping("/admin/api-key")
    public String saveApiKey(@RequestParam(defaultValue = "") String serviceKey,
                             @RequestParam(defaultValue = "save") String action, RedirectAttributes ra) {
        if ("clear".equals(action)) {
            config.remove(G2bApiClient.CONFIG_KEY);
            ra.addFlashAttribute("msg", "\uC800\uC7A5\uB41C \uD0A4\uB97C \uC0AD\uC81C\uD588\uC2B5\uB2C8\uB2E4. (application.yml \uAC12\uC73C\uB85C \uB3CC\uC544\uAC10)");
        } else if (serviceKey.isBlank()) {
            ra.addFlashAttribute("err", "\uD0A4\uB97C \uC785\uB825\uD558\uC138\uC694.");
        } else {
            config.set(G2bApiClient.CONFIG_KEY, serviceKey.trim());
            ra.addFlashAttribute("msg", "API \uD0A4\uB97C \uC800\uC7A5\uD588\uC2B5\uB2C8\uB2E4. \uC7AC\uC2DC\uC791 \uC5C6\uC774 \uBC14\uB85C \uC801\uC6A9\uB429\uB2C8\uB2E4.");
        }
        return "redirect:/admin/api-key";
    }

    @GetMapping("/admin/password") public String password() { return "admin/password"; }

    @PostMapping("/admin/password")
    public String changePassword(Principal p, @RequestParam String current, @RequestParam String next,
                                 @RequestParam String confirm, RedirectAttributes ra) {
        if (next.length() < 8) ra.addFlashAttribute("err", "\uC0C8 \uBE44\uBC00\uBC88\uD638\uB294 8\uC790 \uC774\uC0C1\uC774\uC5B4\uC57C \uD569\uB2C8\uB2E4.");
        else if (!next.equals(confirm)) ra.addFlashAttribute("err", "\uC0C8 \uBE44\uBC00\uBC88\uD638 \uD655\uC778\uC774 \uC77C\uCE58\uD558\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4.");
        else if (!users.changePassword(p.getName(), current, next)) ra.addFlashAttribute("err", "\uD604\uC7AC \uBE44\uBC00\uBC88\uD638\uAC00 \uD2C0\uB838\uC2B5\uB2C8\uB2E4.");
        else ra.addFlashAttribute("msg", "\uBE44\uBC00\uBC88\uD638\uB97C \uBCC0\uACBD\uD588\uC2B5\uB2C8\uB2E4.");
        return "redirect:/admin/password";
    }
}
