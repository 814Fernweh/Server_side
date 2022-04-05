package com.example.project.mapper;

import com.example.project.entity.Record;
import com.sun.javafx.collections.MappingChange;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface RecordMapper {
    int deleteByPrimaryKey(Integer rid);

    int insert(Record record);

    Record selectByPrimaryKey(Integer rid);
    Record selectByEidAndDate(HashMap<String,Object> map);

    List<Record> selectAll();
    List<Record> selectByDepartment(Integer did);

    List<Record> searchByDepartment(HashMap<String,Object> map);
    int updateByPrimaryKey(Record record);
}