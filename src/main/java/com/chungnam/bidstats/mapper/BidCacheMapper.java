package com.chungnam.bidstats.mapper;

import com.chungnam.bidstats.vo.BidNoticeVo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BidCacheMapper {
    int merge(BidNoticeVo vo);
    int countAll();
}
