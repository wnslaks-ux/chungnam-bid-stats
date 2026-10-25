package com.chungnam.bidstats.vo;

import java.util.LinkedHashMap;
import java.util.Map;

public class StatsSummaryVo {
    private String regionName; private int count; private long totalPrice; private long avgPrice;
    private long maxPrice; private long minPrice;
    private Map<String, Long> monthlyTotal = new LinkedHashMap<>();
    private Map<String, Integer> monthlyCount = new LinkedHashMap<>();
    public String getRegionName() { return regionName; } public void setRegionName(String v) { regionName = v; }
    public int getCount() { return count; } public void setCount(int v) { count = v; }
    public long getTotalPrice() { return totalPrice; } public void setTotalPrice(long v) { totalPrice = v; }
    public long getAvgPrice() { return avgPrice; } public void setAvgPrice(long v) { avgPrice = v; }
    public long getMaxPrice() { return maxPrice; } public void setMaxPrice(long v) { maxPrice = v; }
    public long getMinPrice() { return minPrice; } public void setMinPrice(long v) { minPrice = v; }
    public Map<String, Long> getMonthlyTotal() { return monthlyTotal; } public void setMonthlyTotal(Map<String, Long> v) { monthlyTotal = v; }
    public Map<String, Integer> getMonthlyCount() { return monthlyCount; } public void setMonthlyCount(Map<String, Integer> v) { monthlyCount = v; }
}
