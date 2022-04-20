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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


// login URL:  http://127.0.0.1:8081/login.html
@Controller
@Transactional
@RequestMapping(value = "/manager")
public class ManagerController {
    @Resource
    private ManagerService managerService;

    // Initial page to start the program Determine if an administrator is logged in
    @RequestMapping(value = "/initial")
    @ResponseBody
    public void initial(HttpServletRequest request, HttpServletResponse response) {
        try {
            Manager manager = (Manager) request.getSession().getAttribute("manager");
            //If the current user exists, go to the main page
            if (manager != null) {
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
     **Login verification method by entering administrator account password
     * */
    @RequestMapping(value="/mLogin",method = {RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> login(String username, String password) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // System.out.println(username + "===" + password);
        Manager manager = managerService.selectByUsername(username);
        Map<String, Object> map = new HashMap<String, Object>();
        if (manager != null) {
            if (manager.getPwd().equals(password)) {
                    map.put("code", 0);
                    map.put("msg", "login successfully");
                    map.put("data", manager);
                    request.getSession().setAttribute("manager", manager);
            } else {
                    map.put("code", 1);
                    map.put("msg", "Incorrect username or password");
            }
    }
        return map;
    }


}
