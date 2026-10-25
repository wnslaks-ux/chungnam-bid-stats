package com.chungnam.bidstats.mapper;

import com.chungnam.bidstats.vo.RegionVo;
import com.chungnam.bidstats.vo.SearchPresetVo;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SearchPresetMapper {
    List<RegionVo> selectRegions();
    RegionVo selectRegion(String code);
    List<SearchPresetVo> selectList();
    SearchPresetVo selectById(Long id);
    int insert(SearchPresetVo vo);
    int update(SearchPresetVo vo);
    int delete(Long id);
}
