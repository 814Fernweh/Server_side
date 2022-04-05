package com.example.project.service;

import com.example.project.entity.Face;
import com.example.project.mapper.FaceMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;



@Service(value = "faceService")
public class FaceServiceImpl implements FaceService{
    @Resource
    private FaceMapper faceMapper;
    // 添加人脸信息
    @Override
    public int insert(Face face) {
        return faceMapper.insert(face);
    }

    @Override
    public Face selectByEid(Integer eid){
        return faceMapper.selectByEid(eid);
    }
}
