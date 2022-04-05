package com.example.project.controller;

import com.example.project.entity.Manager;
import com.example.project.service.ManagerService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;


// 登录管理员  http://127.0.0.1:8081/login.html
@Controller
@Transactional
@RequestMapping(value = "/manager")//设置访问改控制类的"别名"
public class ManagerController {
    @Resource
    private ManagerService managerService;

    // 启动程序的初始页面 判断是否有管理员登录
    @RequestMapping(value = "/initial")
    @ResponseBody
    public void initial(HttpServletRequest request, HttpServletResponse response) {
        try {
            Manager manager = (Manager) request.getSession().getAttribute("manager");
            //若当前用户存在，进入主页面
            if (manager != null) {
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
    @RequestMapping(value="/mLogin",method = {RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> login(String username, String password) {
        // Map是以键值形式存储数据，有点类似于数组。string是它的键，存储的类型为String
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        System.out.println(username + "===" + password);
        Manager manager = managerService.selectByUsername(username);
    //    System.out.println(manager);
        Map<String, Object> map = new HashMap<String, Object>();
        if (manager != null) {
            if (manager.getPwd().equals(password)) {
                    map.put("code", 0);
                    map.put("msg", "登录成功");
                    map.put("data", manager);
                    request.getSession().setAttribute("manager", manager);
            } else {
                    map.put("code", 1);
                    map.put("msg", "用户名或密码错误");
            }
    }
        return map;
    }


}
