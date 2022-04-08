package com.example.project.service;

import com.example.project.entity.Record;
import com.example.project.entity.Regulation;
import com.example.project.mapper.RegulationMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;


@Service(value = "regulationService")
public class RegulationServiceImpl  implements RegulationService{
    @Resource
    private RegulationMapper regulationMapper;
    @Override
    public int updateByPrimaryKey(Regulation regulation){
        return regulationMapper.updateByPrimaryKey(regulation);
    }
    @Override
    public Regulation selectByPrimaryKey(Integer reid){
        return regulationMapper.selectByPrimaryKey(reid);
    }

}
