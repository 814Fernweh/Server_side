package com.example.project.mapper;

import com.example.project.entity.Employee;
import com.example.project.entity.Record;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface EmployeeMapper {
    int deleteByPrimaryKey(Integer eid);

    int insert(Employee employee);

    Employee selectByPrimaryKey(Integer eid);

    List<Employee> selectAll();

    int updateByPrimaryKey(Employee employee);
    int updatePwdByPrimaryKey(Employee employee);
    Employee selectByTele(String telephone);
    List<Employee> selectByEIDDID(HashMap<String,Object> map);

}
