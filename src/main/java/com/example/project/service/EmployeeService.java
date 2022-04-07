package com.example.project.service;

import com.example.project.entity.Employee;
import com.example.project.entity.Record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface EmployeeService {

    int insert(Employee employee);
    Employee selectByPrimaryKey(Integer eid);
    Employee selectByTele(String telephone);
    // 根据主键eid修改employee
    int updateByPrimaryKey(Employee employee);

    int updatePwdByPrimaryKey(Employee employee);
  //  List<Employee> selectAll();
    Map<String, Object> selectAll();
    Map<String, Object>  selectByEIDDID(HashMap<String,Object> map);
    int deleteByPrimaryKey(Integer eid);
}
