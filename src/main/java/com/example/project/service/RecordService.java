package com.example.project.service;

import com.example.project.entity.Record;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RecordService {
    int insert(Record record);
    Record selectByPrimaryKey(Integer rid);
    int updateByPrimaryKey(Record record);
    Record selectTheArrive(Integer eid, String date);
    Map<String, Object> selectAll();
    Map<String, Object> selectByDepartment(Integer did);
    Map<String, Object>  searchByDepartment(HashMap<String,Object> map);
    int  deleteByPrimaryKey(Integer rid);

}
