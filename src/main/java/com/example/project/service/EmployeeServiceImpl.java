package com.example.project.service;

import com.example.project.entity.Employee;
import com.example.project.entity.Record;
import com.example.project.mapper.EmployeeMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


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


}
