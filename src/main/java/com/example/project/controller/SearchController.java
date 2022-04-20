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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@Transactional
@RequestMapping(value = "/search")
public class SearchController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private RecordService recordService;


    @RequestMapping(value = "/search_init")
    @ResponseBody
    public void search(HttpServletRequest request, HttpServletResponse response) throws IOException {
                ((HttpServletResponse) response).sendRedirect("/searchAll.html");
    }


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
     * Get all dates for a period of time sorted in reverse order
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
            while (calendar.getTime().after(dateOne)) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                dateList.add(sdf.format(calendar.getTime()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateList;
    }


    // query attendance records  https://blog.csdn.net/changyana/article/details/113444574
    @RequestMapping(value = "/list")
    @ResponseBody
    public Map<String, Object> get(int page,int limit,
              @RequestParam(name= "dId",required = false,defaultValue= "") Integer dId,
              @RequestParam(name= "eId",required = false,defaultValue= "") Integer eId,
              @RequestParam(name= "start",required = false,defaultValue= "") String start,
              @RequestParam(name= "end",required = false,defaultValue= "") String end) {
        List<String> dateList = new ArrayList<String>();
        try {

            Map<String,Object> queryMap = new HashMap<String,Object>();
            queryMap.put( "dId",dId);
            queryMap.put( "eId",eId);
            PageHelper.startPage(page, limit);
            Map<String, Object> map0 = recordService.searchByDepartment((HashMap<String, Object>) queryMap);
            List<Record> users = (List<Record>) map0.get("data");
            List<Record> newlist =new ArrayList<>();
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

          //Wrap the results with PageInfo, passing in the number of consecutive pages to be displayed
            Map<String, Object> map =new HashMap<>();
            map.put("data",pageInfo.getList());
            map.put("code", 0);
            map.put("msg", "Success");
            map.put("count", pageInfo.getTotal());
            logger.info("administrator search the records");
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

    @RequestMapping(value = "/listTimeRangeRecord")
    @ResponseBody
    public Map<String, Object> getTimeRangeRecord(int page,int limit,
              @RequestParam(name= "start",required = false,defaultValue= "") String start,
              @RequestParam(name= "end",required = false,defaultValue= "") String end) {

        List<String> dateList = new ArrayList<String>();

        Map<String, Object> res =new HashMap<>();
        try {
            // Find all the dates in the start and end times
            if(!start.equals("") && !end.equals("") ){
                dateList=getTwoDaysDay(start,end);

                //Called before the query, pass in the page number, and the number of pages per page
                PageHelper.startPage(page, limit);
                //startPage后面紧跟的查询是分页查询
                // Once  have found all the data, retrieve the corresponding time in
                // new a List<Record>, put data
                Map<String, Object> all = recordService.selectAll();
                List<Record> alllist= (List<Record>) all.get("data");
                List<Record> newlist =new ArrayList<>();
                for(int j=0;j<alllist.size();j++){
                    for(int z=0;z<dateList.size();z++){
                        if (dateList.get(z).equals(alllist.get(j).getCheckinDate())){
                            newlist.add(alllist.get(j));
                        }
                    }
                }

                PageInfo pageInfo = new PageInfo(newlist,limit);
                res.put("count", pageInfo.getTotal());
                res.put("data",pageInfo.getList());

            }
            else{
                PageHelper.startPage(page, limit);
                Map<String, Object> map0 = recordService.selectAll();
                List<Record> users = (List<Record>) map0.get("data");
                PageInfo pageInfo = new PageInfo(users,limit);
                res.put("data",pageInfo.getList());
                res.put("count", pageInfo.getTotal());
            }

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




    @RequestMapping(value = "/deleteRecord")
    @ResponseBody
    public Map<String, Object> deleteRecord(@RequestParam(name= "rId",required = false,defaultValue= "") Integer rId) {
        try {
            Map<String, Object> res=new HashMap<>();
            recordService.deleteByPrimaryKey(rId);
            Record re;
            re=recordService.selectByPrimaryKey(rId);
            res.put("code", 0);
            res.put("msg", "200");
            logger.info("administrator delete the record of ID ");
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
            logger.info("administrator update the record of ID "+re.geteId()+" on the day of"+ re.getCheckinDate());
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
            logger.info("administrator insert a new record of ID "+re.geteId()+" on the day of "+ re.getCheckinDate());
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


