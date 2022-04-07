package com.example.project.service;

import com.example.project.entity.Employee;
import com.example.project.entity.Record;
import com.example.project.mapper.EmployeeMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service(value = "employeeService")
public class EmployeeServiceImpl implements EmployeeService {
    @Resource
    private EmployeeMapper employeeMapper;

    @Override
    public Employee selectByPrimaryKey(Integer eid){
        return employeeMapper.selectByPrimaryKey(eid);
    }

    @Override
    public Employee selectByTele(String telephone){
        return employeeMapper.selectByTele(telephone);
    }

    @Override
    public int updateByPrimaryKey(Employee employee){
        return employeeMapper.updateByPrimaryKey(employee);
    }
    @Override
    public int updatePwdByPrimaryKey(Employee employee){
        return employeeMapper.updatePwdByPrimaryKey(employee);
    }
    @Override
    public int insert(Employee employee){
        return employeeMapper.insert(employee);
    }

    @Override
    public Map<String, Object> selectAll(){
        Map<String, Object> map=new HashMap<>();
        List<Employee> data=employeeMapper.selectAll();
        map.put("data",data);
        return map;
    }

    @Override
    public Map<String, Object>  selectByEIDDID(HashMap<String,Object> map){
        Map<String, Object> map1=new HashMap<>();
        List<Employee> data=employeeMapper.selectByEIDDID(map);
        map1.put("data",data);
        return map1;
    }

    @Override
    public int deleteByPrimaryKey(Integer eid){
        return employeeMapper.deleteByPrimaryKey(eid);
    }
}
