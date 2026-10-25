package com.chungnam.bidstats.dto;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

/** 검색 폼 / 통계 쿼리 파라미터 */
public class SearchFormDto {
    private String regionCode = "YESAN";
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dateFrom = LocalDate.now().minusMonths(1);
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dateTo = LocalDate.now();
    private Long minPrice; private Long maxPrice; private String keyword;
    public String getRegionCode() { return regionCode; } public void setRegionCode(String v) { regionCode = v; }
    public LocalDate getDateFrom() { return dateFrom; } public void setDateFrom(LocalDate v) { dateFrom = v; }
    public LocalDate getDateTo() { return dateTo; } public void setDateTo(LocalDate v) { dateTo = v; }
    public Long getMinPrice() { return minPrice; } public void setMinPrice(Long v) { minPrice = v; }
    public Long getMaxPrice() { return maxPrice; } public void setMaxPrice(Long v) { maxPrice = v; }
    public String getKeyword() { return keyword; } public void setKeyword(String v) { keyword = v; }
    /** 통계 페이지 링크용 쿼리스트링 */
    public String toQuery() {
        StringBuilder sb = new StringBuilder("regionCode=").append(regionCode)
            .append("&dateFrom=").append(dateFrom).append("&dateTo=").append(dateTo);
        if (minPrice != null) sb.append("&minPrice=").append(minPrice);
        if (maxPrice != null) sb.append("&maxPrice=").append(maxPrice);
        if (keyword != null && !keyword.isEmpty())
            sb.append("&keyword=").append(java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8));
        return sb.toString();
    }
}
