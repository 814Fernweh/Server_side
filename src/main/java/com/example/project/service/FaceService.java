package com.example.project.service;

import com.example.project.entity.Face;

public interface FaceService {

    // 加入人脸数据
    int insert(Face record);
    //
    Face selectByEid(Integer eid);
}
