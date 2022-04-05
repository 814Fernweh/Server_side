package com.example.project.mapper;

import com.example.project.entity.Department;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DepartmentMapper {
    int deleteByPrimaryKey(Integer did);

    int insert(Department record);

    Department selectByPrimaryKey(Integer did);

    List<Department> selectAll();

    int updateByPrimaryKey(Department record);
}