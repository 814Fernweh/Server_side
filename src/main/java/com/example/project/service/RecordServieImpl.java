package com.example.project.service;

import com.example.project.entity.Face;
import com.example.project.entity.Record;
import com.example.project.mapper.FaceMapper;
import com.example.project.mapper.RecordMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service(value = "recordService")
public class RecordServieImpl implements RecordService{
    @Resource
    private RecordMapper recordMapper;
    @Override
    public int insert(Record record) {
        return recordMapper.insert(record);
    }

    @Override
    public Record selectByPrimaryKey(Integer rid){
        return recordMapper. selectByPrimaryKey(rid);
    }

    @Override
    public int updateByPrimaryKey(Record record){
        return recordMapper.updateByPrimaryKey(record);
    }

//    @Override
//    public Record selectByEidAndDate(Integer eid,String date){
//        return recordMapper.selectByEidAndDate(eid,date);
//
//    }

    @Override
    public Record selectTheArrive(Integer eid,String date){

        HashMap<String, Object> map = new HashMap<>();
        map.put("eid",eid);//存放键值对
        map.put("checkinDate",date);//存放键值对
        return recordMapper.selectByEidAndDate(map);

    }
    @Override
    public Map<String, Object> selectAll(){
        Map<String, Object> map=new HashMap<>();
        List<Record> data=recordMapper.selectAll();
        map.put("data",data);
        return map;
    }

    @Override
    public Map<String, Object> selectByDepartment(Integer did){
        Map<String, Object> map=new HashMap<>();
        List<Record> data=recordMapper.selectByDepartment(did);
        map.put("data",data);
        return map;
    }

    @Override
    public Map<String, Object> searchByDepartment(HashMap<String,Object> map){
        Map<String, Object> map1=new HashMap<>();
        List<Record> data=recordMapper.searchByDepartment(map);
        map1.put("data",data);
        return map1;
    }


    @Override
    public  int deleteByPrimaryKey(Integer rid){
        return recordMapper.deleteByPrimaryKey(rid);
    }
}
