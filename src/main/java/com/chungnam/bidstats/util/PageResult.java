package com.chungnam.bidstats.util;

import java.util.List;
import org.springframework.ui.Model;

/** 메모리 페이징 도우미 (페이지 크기 10, 1부터 시작) */
public class PageResult {
    public static final int PAGE_SIZE = 10;

    /** 전체 목록을 받아 현재 페이지만 results 로 넣고 페이징 속성을 모델에 추가 */
    public static <T> void apply(Model m, List<T> all, int page, String query) {
        int total = all == null ? 0 : all.size();
        int totalPages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
        int p = Math.min(Math.max(page, 1), totalPages);
        int from = (p - 1) * PAGE_SIZE, to = Math.min(from + PAGE_SIZE, total);
        int startPage = ((p - 1) / 10) * 10 + 1;
        m.addAttribute("results", total == 0 ? List.of() : all.subList(from, to));
        m.addAttribute("totalCount", total);
        m.addAttribute("page", p);
        m.addAttribute("pageSize", PAGE_SIZE);
        m.addAttribute("totalPages", totalPages);
        m.addAttribute("startPage", startPage);
        m.addAttribute("endPage", Math.min(startPage + 9, totalPages));
        m.addAttribute("hasPrev", p > 1);
        m.addAttribute("hasNext", p < totalPages);
        m.addAttribute("pageQuery", query == null ? "" : query);
    }
}
