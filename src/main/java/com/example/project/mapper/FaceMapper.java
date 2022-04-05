package com.example.project.mapper;

import com.example.project.entity.Face;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FaceMapper {

    int deleteByPrimaryKey(Integer fid);

    // 加入人脸数据
    int insert(Face record);
    // 在check考勤时 根据eid查询face
    Face selectByEid(Integer eid);

    Face selectByPrimaryKey(Integer fid);


    List<Face> selectAll();

    int updateByPrimaryKey(Face record);
}