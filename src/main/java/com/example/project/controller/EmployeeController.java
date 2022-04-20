package com.example.project.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.project.entity.Employee;
import com.example.project.entity.Record;
import com.example.project.service.EmployeeService;
import com.example.project.service.RecordService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Controller
@Transactional
@RequestMapping(value = "/employee")//设置访问改控制类的"别名"
public class EmployeeController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private EmployeeService employeeService;
    @Resource
    private RecordService recordService;
    // Initial page to start the program Determine if an administrator is logged in
    @RequestMapping(value = "/initial")
    @ResponseBody
    public void initial(HttpServletRequest request, HttpServletResponse response) {
        try {
            Employee employee = (Employee) request.getSession().getAttribute("employee");
            //If the current user exists, go to the main page
            if (employee != null) {
                response.setContentType("text/html;charset=utf-8");
                ((HttpServletResponse) response).sendRedirect("/index.html");
            }else {
                // Redirect page to login page for admin login
                ((HttpServletResponse) response).sendRedirect("/login.html");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
     **Login verification method by entering your account password
     * */
    @RequestMapping(value="/employeeLogin",method = {RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> login(String telephone, String password) {

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
       // System.out.println(telephone + "===" + password);
        Employee employee = employeeService.selectByTele(telephone);
    //    System.out.println(employee);
        Map<String, Object> map = new HashMap<String, Object>();
        if (employee!= null) {
            if (employee.getPwd().equals(password)) {
                map.put("code", 0);
                map.put("msg", "login successfully");
                map.put("data", employee);
                request.getSession().setAttribute("employee", employee);
            } else {
                map.put("code", 1);
                map.put("msg", "Incorrect username or password");
            }
        }
        return map;
    }

    @RequestMapping(value="/changePwd",method = {RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> changeEPwd(String ChangePwdData) throws IOException {

        JSONObject weekdata=JSONObject.parseObject(ChangePwdData);
        Integer weekclienteID = weekdata.getInteger("eID");
        String newpwd=weekdata.getString("newpwd");
        Employee e;
        // find the employee
        e=employeeService.selectByPrimaryKey(weekclienteID);
        // First set the new pwd and then update the entire employee data with updatePwdByPrimaryKey
        e.setPwd(newpwd);
        employeeService.updatePwdByPrimaryKey(e);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("pwdMsg","Change password successfully");
        return map;
    }


    @RequestMapping(value = "/listAllEmployee")   //  ,method = RequestMethod.POST
    @ResponseBody
    public Map<String, Object> getEmployee(int page,int limit,
                                   @RequestParam(name= "dId",required = false,defaultValue= "") Integer dId,
                                   @RequestParam(name= "eId",required = false,defaultValue= "") Integer eId) {
        List<String> dateList = new ArrayList<String>();  // date
        try {
            Map<String,Object> queryMap = new HashMap<String,Object>();
            queryMap.put( "dId",dId);
            queryMap.put( "eId",eId);
            //Called before the query, pass in the page number, and the number of pages per page
            PageHelper.startPage(page, limit);
            //The query immediately after startPage is a paging query
            Map<String, Object> map0 = employeeService.selectByEIDDID((HashMap<String, Object>) queryMap);

            List<Employee> users = (List<Employee>) map0.get("data");
            PageInfo pageInfo;
            pageInfo = new PageInfo(users,limit);
            //Wrap the results with PageInfo, passing in the number of consecutive pages to be displayed
            Map<String, Object> map =new HashMap<>();
            map.put("data",pageInfo.getList());
            map.put("code", 0);
            map.put("msg", "Success");
            map.put("count", pageInfo.getTotal());
            logger.info("administrator search the employee information");
            return map;
        } catch (Exception e) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("code", 1);
            map.put("msg", "Server busy");
            map.put("count", 0);
            map.put("data", "[]");
            e.printStackTrace();
            return map;
        }
    }


    @RequestMapping(value = "/deleteEmp")   //  ,method = RequestMethod.POST
    @ResponseBody
    public Map<String, Object> deleteEmp(@RequestParam(name= "eId",required = false,defaultValue= "") Integer eId) {
        try {
            Map<String, Object> res=new HashMap<>();
            employeeService.deleteByPrimaryKey(eId);
            res.put("code", 0);
            res.put("msg", "200");
            logger.info("administrator delete the employee of ID "+ eId);
//            System.out.println(res);
            return res;
        } catch (Exception e) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("code", 1);
            map.put("msg", "Server busy");
            e.printStackTrace();
            return map;
        }
    }

}
