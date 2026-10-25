package com.chungnam.bidstats.service;

import com.chungnam.bidstats.config.Regions;
import com.chungnam.bidstats.mapper.SearchPresetMapper;
import com.chungnam.bidstats.vo.RegionVo;
import com.chungnam.bidstats.vo.SearchPresetVo;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {
    private final SearchPresetMapper mapper;
    public SettingsService(SearchPresetMapper mapper) { this.mapper = mapper; }

    public List<RegionVo> regions() { return mapper.selectRegions(); }
    public List<SearchPresetVo> list() { return mapper.selectList(); }
    public SearchPresetVo get(Long id) { return mapper.selectById(id); }

    @Transactional
    public void save(SearchPresetVo vo) {
        vo.setRegionName(Regions.nameOf(vo.getRegionCode()));
        if (vo.getId() == null) mapper.insert(vo); else mapper.update(vo);
    }
    @Transactional
    public void delete(Long id) { mapper.delete(id); }
}
