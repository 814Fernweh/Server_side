package com.example.project.mapper;

import com.example.project.entity.Regulation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RegulationMapper {
    int deleteByPrimaryKey(Integer reid);

    int insert(Regulation record);

    Regulation selectByPrimaryKey(Integer reid);

    List<Regulation> selectAll();

    int updateByPrimaryKey(Regulation record);
}