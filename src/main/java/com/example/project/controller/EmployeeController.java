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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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

@Controller
@Transactional
@RequestMapping(value = "/employee")//设置访问改控制类的"别名"
public class EmployeeController {
    @Resource
    private EmployeeService employeeService;
    @Resource
    private RecordService recordService;
    // 启动程序的初始页面 判断是否有管理员登录
    @RequestMapping(value = "/initial")
    @ResponseBody
    public void initial(HttpServletRequest request, HttpServletResponse response) {
        try {
            Employee employee = (Employee) request.getSession().getAttribute("employee");
            //若当前用户存在，进入主页面
            if (employee != null) {
                response.setContentType("text/html;charset=utf-8");
                ((HttpServletResponse) response).sendRedirect("/index.html");
            }else {
                // 重定向页面跳转 登录页面 进行管理员登录
                ((HttpServletResponse) response).sendRedirect("/login.html");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
     **输入账号密码登录校验方法
     * */
    @RequestMapping(value="/employeeLogin",method = {RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> login(String telephone, String password) {
        // Map是以键值形式存储数据，有点类似于数组。string是它的键，存储的类型为String
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        System.out.println(telephone + "===" + password);
        Employee employee = employeeService.selectByTele(telephone);
        System.out.println(employee);
        Map<String, Object> map = new HashMap<String, Object>();
        if (employee!= null) {
            if (employee.getPwd().equals(password)) {
                map.put("code", 0);
                map.put("msg", "登录成功");
                map.put("data", employee);
                request.getSession().setAttribute("employee", employee);
            } else {
                map.put("code", 1);
                map.put("msg", "用户名或密码错误");
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
        // 找到那个employee
        e=employeeService.selectByPrimaryKey(weekclienteID);
        // 先set新的pwd  然后用updatePwdByPrimaryKey来更新整条employee数据
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
        List<String> dateList = new ArrayList<String>();  // 放日期的
        try {
            Map<String,Object> queryMap = new HashMap<String,Object>();
            queryMap.put( "dId",dId);  // 没错 可以正确查询 并显示在table里面
            queryMap.put( "eId",eId);
            //查询之前调用，传入页码，以及每页数量
            PageHelper.startPage(page, limit);
            //startPage后面紧跟的查询是分页查询
            Map<String, Object> map0 = employeeService.selectByEIDDID((HashMap<String, Object>) queryMap);

            List<Employee> users = (List<Employee>) map0.get("data");
           // List<Employee> newlist =new ArrayList<>();  // 放指定日期范围内的record
            PageInfo pageInfo;

            pageInfo = new PageInfo(users,limit);

            //用PageInfo对结果进行包装,传入连续显示的页数
            Map<String, Object> map =new HashMap<>();
            map.put("data",pageInfo.getList());
            map.put("code", 0);
            map.put("msg", "Success");
            map.put("count", pageInfo.getTotal());
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



}
