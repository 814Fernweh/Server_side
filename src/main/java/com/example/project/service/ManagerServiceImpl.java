package com.example.project.service;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.example.project.entity.Manager;
import com.example.project.mapper.ManagerMapper;

@Service(value = "managerService")
public class ManagerServiceImpl implements ManagerService {

    @Resource
    private ManagerMapper managerMapper;

    @Override
    public Manager selectByUsername(String username){
        return managerMapper.selectByUsername(username);
    }
//    @Override
//    public Manager loginPage(String username, String password) {
//        return managerMapper.loginPage(username,password);
//    }

}

//    @Override
//    public int del(int id) {
//        return managerMapper.del(id);
//    }


