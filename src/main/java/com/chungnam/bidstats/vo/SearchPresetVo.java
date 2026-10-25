package com.chungnam.bidstats.vo;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class SearchPresetVo {
    private Long id; private String presetName; private String regionCode; private String regionName;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dateFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dateTo;
    private Long minPrice; private Long maxPrice; private String keyword;
    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public String getPresetName() { return presetName; } public void setPresetName(String v) { presetName = v; }
    public String getRegionCode() { return regionCode; } public void setRegionCode(String v) { regionCode = v; }
    public String getRegionName() { return regionName; } public void setRegionName(String v) { regionName = v; }
    public LocalDate getDateFrom() { return dateFrom; } public void setDateFrom(LocalDate v) { dateFrom = v; }
    public LocalDate getDateTo() { return dateTo; } public void setDateTo(LocalDate v) { dateTo = v; }
    public Long getMinPrice() { return minPrice; } public void setMinPrice(Long v) { minPrice = v; }
    public Long getMaxPrice() { return maxPrice; } public void setMaxPrice(Long v) { maxPrice = v; }
    public String getKeyword() { return keyword; } public void setKeyword(String v) { keyword = v; }
}
