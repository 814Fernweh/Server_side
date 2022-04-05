package com.example.project.controller;

import com.example.project.entity.Record;
import com.example.project.service.RecordService;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.github.pagehelper.PageHelper;

@Controller
@Transactional
@RequestMapping(value = "/search")//设置访问改控制类的"别名"
public class SearchController {

    @Resource
    private RecordService recordService;

    // 启动程序的初始页面 判断是否有管理员登录
    @RequestMapping(value = "/search_init")
    @ResponseBody
    public void search(HttpServletRequest request, HttpServletResponse response) throws IOException {
                ((HttpServletResponse) response).sendRedirect("/searchAll.html");
    }

    //   展示所有考勤记录
    @RequestMapping("/searchAll")
    @ResponseBody
    public Map<String, Object> searchAll(int page,int limit
              ) {

        try {
            //查询之前调用，传入页码，以及每页数量
            PageHelper.startPage(page, limit);
            //startPage后面紧跟的查询是分页查询
            Map<String, Object> map0 = recordService.selectAll();

            List<Record> users = (List<Record>) map0.get("data");

            //用PageInfo对结果进行包装,传入连续显示的页数
            PageInfo pageInfo = new PageInfo(users,limit);

            Map<String, Object> map = new HashMap<>();   // 改了 ecordService.selectAll();
            map.put("data",pageInfo.getList());
            map.put("code", 0);
            map.put("msg", "");
            map.put("count", pageInfo.getTotal());
            return map;
        } catch (Exception e) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("code", 1);
            map.put("msg", "服务器繁忙");
            map.put("count", 0);
            map.put("data", "[]");
            e.printStackTrace();
            return map;
        }
    }

    /**
     * 根据一段时间获取该段时间的所有日期  倒序排序
     * @param startDate
     * @param endDate
     * @return yyyy-MM-dd
     */
    // https://blog.csdn.net/weixin_44046583/article/details/120438380
    public static List<String> getTwoDaysDay(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<String> dateList = new ArrayList<String>();
        try {
            Date dateOne = sdf.parse(startDate);
            Date dateTwo = sdf.parse(endDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateTwo);

            dateList.add(endDate);
            while (calendar.getTime().after(dateOne)) { //倒序时间,顺序after改before其他相应的改动。
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                dateList.add(sdf.format(calendar.getTime()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateList;
    }


    // 按部门查询  https://blog.csdn.net/changyana/article/details/113444574
    @RequestMapping(value = "/list")   //  ,method = RequestMethod.POST
    @ResponseBody
    public Map<String, Object> get(int page,int limit,
              @RequestParam(name= "dId",required = false,defaultValue= "") Integer dId,
              @RequestParam(name= "eId",required = false,defaultValue= "") Integer eId,
              @RequestParam(name= "start",required = false,defaultValue= "") String start,
              @RequestParam(name= "end",required = false,defaultValue= "") String end) {
        List<String> dateList = new ArrayList<String>();  // 放日期的
        try {
            Map<String,Object> queryMap = new HashMap<String,Object>();
            queryMap.put( "dId",dId);  // 没错 可以正确查询 并显示在table里面
            queryMap.put( "eId",eId);
            //查询之前调用，传入页码，以及每页数量
            PageHelper.startPage(page, limit);
            //startPage后面紧跟的查询是分页查询
            Map<String, Object> map0 = recordService.searchByDepartment((HashMap<String, Object>) queryMap);
            List<Record> users = (List<Record>) map0.get("data");
            List<Record> newlist =new ArrayList<>();  // 放指定日期范围内的record
            PageInfo pageInfo;
            if(!start.equals("") && !end.equals("") ) {
                dateList = getTwoDaysDay(start, end);

                for(int j=0;j<users.size();j++){
                    for(int z=0;z<dateList.size();z++){
                        if (dateList.get(z).equals(users.get(j).getCheckinDate())){
                            newlist.add(users.get(j));
                        }
                    }
                }
                pageInfo = new PageInfo(newlist,limit);
            }
            else{
                pageInfo = new PageInfo(users,limit);
            }

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

    @RequestMapping(value = "/listTimeRangeRecord")   //  ,method = RequestMethod.POST
    @ResponseBody
    public Map<String, Object> getTimeRangeRecord(int page,int limit,
              @RequestParam(name= "start",required = false,defaultValue= "") String start,
              @RequestParam(name= "end",required = false,defaultValue= "") String end) {

        List<String> dateList = new ArrayList<String>();

        Map<String, Object> res =new HashMap<>(); // 放最终结果的
        try {
            // 找到在起止时间里面的所有日期
            if(!start.equals("") && !end.equals("") ){
                dateList=getTwoDaysDay(start,end);

                //查询之前调用，传入页码，以及每页数量
                PageHelper.startPage(page, limit);
                //startPage后面紧跟的查询是分页查询
                // 找到所有数据后 在里面取出对应时间的  放到一个新的List<Record>中 再put成data
                Map<String, Object> all = recordService.selectAll();
                List<Record> alllist= (List<Record>) all.get("data");
                List<Record> newlist =new ArrayList<>();  // 放指定日期范围内的record
                for(int j=0;j<alllist.size();j++){
                    for(int z=0;z<dateList.size();z++){
                        if (dateList.get(z).equals(alllist.get(j).getCheckinDate())){
                            newlist.add(alllist.get(j));
                        }
                    }
                }

                //用PageInfo对结果进行包装,传入连续显示的页数
                PageInfo pageInfo = new PageInfo(newlist,limit);
                res.put("count", pageInfo.getTotal());
                res.put("data",pageInfo.getList());

            }
            else{
                //查询之前调用，传入页码，以及每页数量
                PageHelper.startPage(page, limit);
                //startPage后面紧跟的查询是分页查询
                Map<String, Object> map0 = recordService.selectAll();
                List<Record> users = (List<Record>) map0.get("data");
                //用PageInfo对结果进行包装,传入连续显示的页数
                PageInfo pageInfo = new PageInfo(users,limit);

                res.put("data",pageInfo.getList());
                res.put("count", pageInfo.getTotal());
            }

            // 最终返回的map结果
            res.put("code", 0);
            res.put("msg", "Success");
            System.out.println(res);
            return res;
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




    @RequestMapping(value = "/deleteRecord")   //  ,method = RequestMethod.POST
    @ResponseBody
    public Map<String, Object> deleteRecord(@RequestParam(name= "rId",required = false,defaultValue= "") Integer rId) {
        try {
            Map<String, Object> res=new HashMap<>();
            recordService.deleteByPrimaryKey(rId);
            res.put("code", 0);
            res.put("msg", "200");
            System.out.println(res);
            return res;
        } catch (Exception e) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("code", 1);
            map.put("msg", "Server busy");
            e.printStackTrace();
            return map;
        }
    }


    @RequestMapping(value = "/updateRecord")   //  ,method = RequestMethod.POST
    @ResponseBody
    public Map<String, Object> updateRecord(@RequestBody Record re) {
        try {
            Map<String, Object> res=new HashMap<>();
            recordService.updateByPrimaryKey(re);
            res.put("code", 0);
            res.put("msg", "200");
            System.out.println(res);
            return res;
        } catch (Exception e) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("code", 1);
            map.put("msg", "Server busy");
            e.printStackTrace();
            return map;
        }
    }

    @RequestMapping(value = "/insertRecord")   //  ,method = RequestMethod.POST
    @ResponseBody
    public Map<String, Object> insertRecord(@RequestBody Record re) {
        try {
            Map<String, Object> res=new HashMap<>();
            recordService.insert(re);
            res.put("code", 0);
            res.put("msg", "200");
            System.out.println(res);
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


