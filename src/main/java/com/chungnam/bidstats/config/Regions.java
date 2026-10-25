package com.chungnam.bidstats.config;

import java.util.LinkedHashMap;
import java.util.Map;

/** 대상 시·군 상수. 지역 추가 시 여기와 data.sql 의 region 테이블을 함께 수정하세요. */
public final class Regions {
    public static final Map<String, String> ALL = new LinkedHashMap<>();
    static {
        ALL.put("YESAN", "\uC608\uC0B0\uAD70");           // 예산군
        ALL.put("HONGSEONG", "\uD64D\uC131\uAD70");       // 홍성군
        ALL.put("SEOSAN", "\uC11C\uC0B0\uC2DC");           // 서산시
        ALL.put("TAEAN", "\uD0DC\uC548\uAD70");            // 태안군
        ALL.put("BUYEO", "\uBD80\uC5EC\uAD70");            // 부여군
        ALL.put("NONSAN", "\uB17C\uC0B0\uC2DC");           // 논산시
        ALL.put("CHEONGYANG", "\uCCAD\uC591\uAD70");      // 청양군
    }
    private Regions() {}
    public static String nameOf(String code) { return ALL.getOrDefault(code, code); }
}
