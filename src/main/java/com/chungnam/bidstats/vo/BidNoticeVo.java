package com.chungnam.bidstats.vo;

/** 나라장터 공사 입찰공고 1건 */
public class BidNoticeVo {
    private String bidNtceNo; private String bidNtceNm; private String dminsttNm;
    private String bidNtceDt; private Long presmptPrce; private String bidNtceUrl;
    public String getBidNtceNo() { return bidNtceNo; } public void setBidNtceNo(String v) { bidNtceNo = v; }
    public String getBidNtceNm() { return bidNtceNm; } public void setBidNtceNm(String v) { bidNtceNm = v; }
    public String getDminsttNm() { return dminsttNm; } public void setDminsttNm(String v) { dminsttNm = v; }
    public String getBidNtceDt() { return bidNtceDt; } public void setBidNtceDt(String v) { bidNtceDt = v; }
    public Long getPresmptPrce() { return presmptPrce; } public void setPresmptPrce(Long v) { presmptPrce = v; }
    public String getBidNtceUrl() { return bidNtceUrl; } public void setBidNtceUrl(String v) { bidNtceUrl = v; }
}
