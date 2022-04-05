package com.example.project.service;

import com.example.project.entity.Manager;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

public interface ManagerService {

  //  Manager loginPage(String username, String password);

    Manager selectByUsername(String username);
    // 只有mapper  需要@param
    //   Manager loginPage(@Param("username")String username, @Param("password")String password);

    //int addManager(Manager manager);

    //int del (int id);


}


