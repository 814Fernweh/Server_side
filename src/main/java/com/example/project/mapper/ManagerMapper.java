package com.example.project.mapper;

import com.example.project.entity.Manager;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

import org.apache.ibatis.annotations.Select;

@Mapper //声明是一个Mapper,与springbootApplication中的@MapperScan二选一写上即可
public interface ManagerMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Manager record);

    Manager selectByPrimaryKey(Integer id);

    List<Manager> selectAll();

    Manager selectByUsername(String username);

    int updateByPrimaryKey(Manager record);

 //   Manager loginPage(String username,String password);

    // 只有mapper  需要@param
  //  Manager loginPage(@Param("username")String username, @Param("password")String password);

 //   Manager loginPage(Map<String, String> String username);
}




    //@Insert("INSERT INTO `manager`(`id`,`username`,`pwd`) " +
    //         "VALUES (1, #{username},#{pwd});")
    //int addManager(Manager manager);

    //@Delete("delete from manager where id = #{id}")
    ///int del (int id);

