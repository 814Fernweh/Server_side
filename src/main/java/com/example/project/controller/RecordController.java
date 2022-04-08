package com.example.project.controller;

import com.alibaba.fastjson.JSONObject;
import com.arcsoft.face.*;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.DetectOrient;
import com.arcsoft.face.enums.ErrorInfo;
import com.arcsoft.face.toolkit.ImageInfo;
import com.example.project.entity.Employee;
import com.example.project.entity.Face;
import com.example.project.entity.Record;
import com.example.project.service.EmployeeService;
import com.example.project.service.FaceService;
import com.example.project.service.RecordService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.arcsoft.face.toolkit.ImageFactory.getRGBData;

// 用客户端的照片 做活体检测（不是翻拍） 和人脸对比
// 安卓app有权限管理 可以禁止访问相册等软件
@Controller
@Transactional
@RequestMapping(value = "/attendance")//设置访问改控制类的"别名"
//给安卓客户端 调用的服务器接口 客户端至少3个接口 登录 考勤 查询考勤信息
public class RecordController {

    @Resource
    private FaceService faceService;
    @Resource
    private EmployeeService employeeService;
    @Resource
    private RecordService recordService;

    //reference: https://www.it610.com/article/1293097618573959168.htm   判断经纬度的
    private static double PI = 3.14159265;
    private static double EARTH_RADIUS = 6378137;
    private static double RAD = Math.PI / 180.0;
    double clientLati,clientLong;
    Integer clienteID,clientType;
    Integer weekclienteID,flag;


    @RequestMapping(value="/getData",method = {RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> getJsonData(String JsonData) throws IOException, ParseException {
        String jpegFilename=null;

        JSONObject initData=JSONObject.parseObject(JsonData);
        clientLati= Double.valueOf(initData.getString("Latitude"));
        clientLong= Double.valueOf(initData.getString("Longitude"));
        String  clientTele=initData.getString("eTele");
        clienteID=initData.getInteger("eID");
        clientType=initData.getInteger("type");
        OutputStream outputStream = null;
        try {
            // 解密处理数据
            byte[] bytes = new BASE64Decoder().decodeBuffer(initData.get("jpegData").toString());
            Date checkinDate=new Date();
            SimpleDateFormat format=new SimpleDateFormat("yyyyMMddHHmmss");
            jpegFilename="D:\\0-YEAR4\\InividualProject\\project\\"+clientTele+format.format(checkinDate)+".jpg";
            outputStream = new FileOutputStream(jpegFilename);
            // 写入图片
            outputStream.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (outputStream != null) {
                try {
                    // 关闭outputStream流
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        int recordResult=addRecord(clientType,clienteID,clientLong,clientLati,jpegFilename);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("resultCode",recordResult);
        String msg;
        switch (recordResult){
            case 0:
                msg="Attendance Success!";
                break;
            case 1:
                msg="Late Arrival!";
                break;
            case 2:
                msg="Early Departure!";
                break;
            case 3:
                msg="Already clocked in at work";
                break;
            case -1:
                msg="Far from your correct location";
                break;
            case -2:
                msg="Can not use picture";
                break;
            case -3:
                msg="The similarity is too low";
                break;
            case -4:
                msg="some error in the checkin";
                break;
            default:
                msg="error";
                break;
        }
        map.put("msg",msg);
        return map;
    }


    /**
     * 获取过去第几天的日期
     *
     * @param past
     * @return
     */
    public static String getPastDate(int past,Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - past);
        Date today = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String result = sdf.format(today);
        return result;
    }


    // 安卓端查询考勤数据 可以默认按周查询
    //week暂时按当前日期-7； month可以取上个月的1号和本月1日-1；
    //没有考勤记录的日子 是只显示日期
    @RequestMapping(value="/clientSearchWeekData",method = {RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> getWeek(String SearchWeekData) throws IOException {

        JSONObject weekdata=JSONObject.parseObject(SearchWeekData);
        weekclienteID=weekdata.getInteger("eID");
        flag=weekdata.getInteger("flag");
        ArrayList<Record> weekRecord;
        Record r;

        // 获取前几天的日期
        ArrayList<String> pastDaysList = new ArrayList<>();
        try {
            Date today = new Date(); // 今天！
            DateFormat day = DateFormat.getDateInstance() ;
            //我这里传来的时间是个string类型的，所以要先转为date类型的
            SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
            Date date =sdf.parse(day.format(today));
            for (int i = 6; i >= 0; i--) {
                pastDaysList.add(getPastDate(i,date));
            }
        }catch (ParseException e){
            e.printStackTrace();
        }
        // 获取到一周的日期了  String格式的
        Map<String, Object> map = new HashMap<String, Object>();
       // record.setCheckinDate(day.format(date1));   // 在考勤记录中 添加日期
        for (int i=0;i<7;i++){
            //获取每个日期的record
            r=recordService.selectTheArrive(weekclienteID,pastDaysList.get(i));
            // 找到记录了  把考勤的具体时间传给客户端
            if(r!=null) {
                if(r.getArriveTime()!=null && r.getLeaveTime()!=null ){
                    map.put(pastDaysList.get(i)+",arrive",r.getArriveTime());
                    map.put(pastDaysList.get(i)+",leave",r.getLeaveTime());
                    map.put(pastDaysList.get(i)+",re",r.getResult());
                }
                else if(r.getArriveTime()==null && r.getLeaveTime()!=null  ){
                    map.put(pastDaysList.get(i)+",arrive","no arrive");
                    map.put(pastDaysList.get(i)+",leave",r.getLeaveTime());
                    map.put(pastDaysList.get(i)+",re",r.getResult());
                }

                else if(r.getArriveTime()!=null && r.getLeaveTime()==null){
                    map.put(pastDaysList.get(i)+",arrive",r.getArriveTime());
                    map.put(pastDaysList.get(i)+",leave","no leave");
                    map.put(pastDaysList.get(i)+",re",r.getResult());
                }
            }
            else{
                map.put(pastDaysList.get(i)+",arrive","no arrive");
                map.put(pastDaysList.get(i)+",leave","no leave");
                map.put(pastDaysList.get(i)+",re","0");
            }
        }
        return map;
    }

    @RequestMapping(value="/clientSearchMonthData",method = {RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> getMonth(String SearchMonthData) throws IOException {

        JSONObject weekdata=JSONObject.parseObject(SearchMonthData);
        weekclienteID=weekdata.getInteger("eID");
        flag=weekdata.getInteger("flag");
        ArrayList<Record> weekRecord;
        Record r;


        // 获取前几天的日期
        ArrayList<String> pastDaysList = new ArrayList<>();
        try {
            Date today = new Date(); // 今天！
            DateFormat day = DateFormat.getDateInstance() ;
            //我这里传来的时间是个string类型的，所以要先转为date类型的
            SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
            Date date =sdf.parse(day.format(today));
            for (int i = 30; i >= 0; i--) {
                pastDaysList.add(getPastDate(i,date));
            }
        }catch (ParseException e){
            e.printStackTrace();
        }
        // 获取到一个月的日期了  String格式的
        Map<String, Object> map = new HashMap<String, Object>();
        // record.setCheckinDate(day.format(date1));   // 在考勤记录中 添加日期
        for (int i=0;i<31;i++){
            //获取每个日期的record
            r=recordService.selectTheArrive(weekclienteID,pastDaysList.get(i));
            // 找到记录了  把考勤的具体时间传给客户端
            if(r!=null) {
                if(r.getArriveTime()!=null && r.getLeaveTime()!=null ){
                    map.put(pastDaysList.get(i)+",arrive",r.getArriveTime());
                    map.put(pastDaysList.get(i)+",leave",r.getLeaveTime());
                    map.put(pastDaysList.get(i)+",re",r.getResult());
                }
                else if(r.getArriveTime()==null && r.getLeaveTime()!=null  ){
                    map.put(pastDaysList.get(i)+",arrive","no arrive");
                    map.put(pastDaysList.get(i)+",leave",r.getLeaveTime());
                    map.put(pastDaysList.get(i)+",re",r.getResult());
                }

                else if(r.getArriveTime()!=null && r.getLeaveTime()==null){
                    map.put(pastDaysList.get(i)+",arrive",r.getArriveTime());
                    map.put(pastDaysList.get(i)+",leave","no leave");
                    map.put(pastDaysList.get(i)+",re",r.getResult());
                }
            }
            else{
                map.put(pastDaysList.get(i)+",arrive","no arrive");
                map.put(pastDaysList.get(i)+",leave","no leave");
                map.put(pastDaysList.get(i)+",re","0");
            }
        }
        return map;
    }



    /// 根据提供的两套经纬度计算距离(米) 误差10米 多了10米  在isInCircle里面调用它
    public static double getDistance(double lng1,double lat1, double lng2, double lat2)
    {
        double radLat1 = lat1 * RAD;
        double radLat2 = lat2 * RAD;
        double a = radLat1 - radLat2;
        double b = (lng1 - lng2) * RAD;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }


    // 判断一个点是否在圆形区域内
    public static boolean isInCircle(double lng1,double lat1, double lng2, double lat2, String radius) {
        double distance = getDistance(lng1, lat1, lng2,lat2);
        System.out.println(distance);
        double r = Double.parseDouble(radius);
        System.out.println(r);
        if (distance > r) {
            return false;
        } else {
            return true;
        }
    }

    // 判断时间 上班or下班or 不在考勤时间内 在查询统计时再判断、输出 不存在数据库里了
    public int addRecord(Integer type,Integer eid,double longitude,double latitude,String filename) throws ParseException {
            // 不能删掉的！！！！！！判斷數據庫有無記錄 上班
//            Record re;
//            Date date = new Date();
//            DateFormat day = DateFormat.getDateInstance() ;//日期格式，精确到日
//            String s1=  day.format (date);
//            System.out.println (s1 ) ;
//            re=recordService.selectTheArrive(eid, s1);
//            if (re!=null){
//                return -5; // 已經有數據
//            }


        int ca=checkAttendance(eid,longitude,latitude,filename);
        if(ca!=0){
            return ca;
        }
        Employee e;
        // 考勤成功了
        if(ca==0){
            // 查询有没有这条考勤记录
                Record re;
                //中国时间日期
                SimpleDateFormat df00 = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
                String s00=df00.format(new Date());   // new Date()为获取当前系统时间
                System.out.println (s00) ;

                re=recordService.selectTheArrive(eid, s00); // 查询有没有这条考勤记录

            // 获取系统时间
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            // 22:3
            String now=hour+":"+minute;

            String standardArriveTime="09:00";
            String standardLeaveTime="18:00";
            java.text.DateFormat df=new java.text.SimpleDateFormat("HH:mm");

            java.util.Calendar c1=java.util.Calendar.getInstance();
            java.util.Calendar c2=java.util.Calendar.getInstance();
            java.util.Calendar c3=java.util.Calendar.getInstance();
            try
            {
                c1.setTime(df.parse(now));
                c2.setTime(df.parse(standardArriveTime));
                c3.setTime(df.parse(standardLeaveTime));

            }catch(java.text.ParseException e1){
                System.err.println("Wrong format");
            }
            // 将当前时间和上下班规定时间做比较
            int resultA=c1.compareTo(c2);
            int resultL=c1.compareTo(c3);
            e = employeeService.selectByPrimaryKey(eid);  //找到那个employee
            // 今天未打卡   不存在考勤记录 这是第一次打卡
                if(re==null){
                    Record record = new Record();
                    record.seteId(eid);
                    record.setdId(e.getdId());
                    record.setName(e.getName());  // 员工姓名
                    // 获取日期 String
                    SimpleDateFormat df0 = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
                    String s=df0.format(new Date());   // new Date()为获取当前系统时间
                    System.out.println (s) ;
                    record.setCheckinDate(s);  //添加考勤的日期

                    if(resultA==0 || resultA<0 ) {
                        // c1相等c2  c1小于c2   时间早于上班时间
                        // 上班 正常 add record

                        DateFormat dateFormatterChina = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM);//格式化输出
                        TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai");//获取时区
                        dateFormatterChina.setTimeZone(timeZoneChina);//设置系统时区
                        Date curDate = new Date();//获取系统时间

                        record.setArriveTime(curDate);    //添加上班时间
                        record.setAResult("normal");
                        record.setResult("0");    // 上班正常 没有下班记录
                        recordService.insert(record);
                        System.out.print("1.Successful insertion of new work data\n");
                        return 0;
                    }

                    if(resultL==0 || resultL>0 ) {
                        // c1相等c3 c1大于c3
                        //下班正常 上班没打卡！
                        record.setLeaveTime(new Date());    //添加下班时间
                        record.setLResult("normal");
                        record.setResult("0");
                        recordService.insert(record);
                        System.out.print("2.Successful insertion of new work data\n");
                        return 0;
                    }
                    if(resultA>0 || resultL<0 ) {
                        // c1大于c2 c1小于c3
                        // 上班迟到了
                        record.setArriveTime(new Date());    //添加上班时间
                        record.setAResult("late arrival");
                        record.setResult("0");   // 上班迟到了
                        recordService.insert(record);
                        System.out.print("3.Successful insertion of new work data\n");
                        return 1;
                    }
                }

                // 今天已经打卡了  有一条考勤记录
                else{
                    Record updateRe;
                    updateRe=recordService.selectTheArrive(eid, s00);
                    //获取当前时间 作为下班打卡时间
                    Date date2 = new Date();
                    if(resultA==0 || resultA<0 ) {
                        // c1相等c2  c1小于c2   时间早于上班时间
                        // 不更新
                        return 3;

                    }
                    if(resultL==0 || resultL>0 ) {
                        // c1相等c3 c1大于c3   时间晚于下班时间
                        //下班正常  上班也打卡了  update此条record

                        updateRe.setLeaveTime(date2);  //下班打卡时间
                        updateRe.setLResult("normal");
                        if(updateRe.getAResult().equals("normal")){
                            updateRe.setResult("1");
                        }
                        else {
                            updateRe.setResult("0");
                        }
                        recordService.updateByPrimaryKey(updateRe);
                        return 0;
                    }
                    if(resultA>0 || resultL<0 ) {
                        // c1大于c2 c1小于c3   晚于上班时间 早于下班时间
                        // 下班早退  上班打卡过  update record
                        updateRe.setLeaveTime(date2);  //下班打卡时间
                        updateRe.setLResult("early departure");
                        updateRe.setResult("0");
                        recordService.updateByPrimaryKey(updateRe);
                        return 2;

                    }
                }
        }
//            // 获取当前日期时间  https://www.cnblogs.com/east7/p/15389080.html
        return -4;             //其他错误   考勤失败 提示重新考勤

    }

    // 客户端传入 employee的id 地理位置 人脸照片 与数据库里存的数据对比
    // 返回数字 再根据数字判断 是更新record还是不更新  加入当前日期、时间 作为考勤时间
    // return0 说明考勤成功需要加一条record  return 1说明失败，提示用户重新考勤
    public int checkAttendance(Integer eid, double longitude, double latitude, String filename) {

        // 先判断位置对不对
        Employee employee;
        employee = employeeService.selectByPrimaryKey(eid);

        // 从数据库里面取出地理位置值
        double stan_longitude = employee.getLongitude().doubleValue();
        double stan_latitude = employee.getLatitude().doubleValue();
        // 在半径为500m的圆内
        String radius = "800";
        boolean withinLocation = isInCircle(stan_longitude, stan_latitude, longitude, latitude, radius);
        if (!withinLocation)
        {
            return -1;  // 位置不对
        }
            // 位置对了 进行活体检测 判断是不是真人操作
        boolean isAlive=checkLiveness(filename);
        if(!isAlive)
        {
            return -2;   // 不是活体
        }
        // 是活体 继续进行人脸相似度对比
        boolean isSamePerson=similarity(eid,filename);
        if(!isSamePerson)
        {
            return -3;   // 人脸相似度不够
        }
        return 0;

    }
        // 客户端传入的人脸照 首先判断是不是活体
    public boolean checkLiveness(String filename){
         //   filename = "C:\\Users\\zhuling\\Desktop\\wlm.jpg";

            String appId = "CDHewBaWcAy2uz4Mn8raaWneBjC7C7wUYaotz2Enx3Hz";
            String sdkKey = "AQD5PgF41UwbfD59A1ncZfdBHtd1XCAV8MhNaASdNZ4F";
            FaceEngine faceEngine = new FaceEngine("D:\\0-YEAR4\\InividualProject\\arcsoft_lib");
            //激活引擎
            int errorCode = faceEngine.activeOnline(appId, sdkKey);
            if (errorCode != ErrorInfo.MOK.getValue() && errorCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()) {
                System.out.println("引擎激活失败");
            }

            ActiveFileInfo activeFileInfo = new ActiveFileInfo();
            errorCode = faceEngine.getActiveFileInfo(activeFileInfo);
            if (errorCode != ErrorInfo.MOK.getValue() && errorCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()) {
                System.out.println("获取激活文件信息失败");
            }

            //引擎配置
            EngineConfiguration engineConfiguration = new EngineConfiguration();
            engineConfiguration.setDetectMode(DetectMode.ASF_DETECT_MODE_IMAGE);
            engineConfiguration.setDetectFaceOrientPriority(DetectOrient.ASF_OP_ALL_OUT);
            engineConfiguration.setDetectFaceMaxNum(10);
            engineConfiguration.setDetectFaceScaleVal(16);
            //功能配置
            FunctionConfiguration functionConfiguration = new FunctionConfiguration();
            functionConfiguration.setSupportAge(true);
            functionConfiguration.setSupportFace3dAngle(true);
            functionConfiguration.setSupportFaceDetect(true);
            functionConfiguration.setSupportFaceRecognition(true);
            functionConfiguration.setSupportGender(true);

            functionConfiguration.setSupportLiveness(true);

            functionConfiguration.setSupportIRLiveness(true);
            engineConfiguration.setFunctionConfiguration(functionConfiguration);

            //初始化引擎
            errorCode = faceEngine.init(engineConfiguration);
            if (errorCode != ErrorInfo.MOK.getValue()) {
                System.out.println("初始化引擎失败");
            }
            //人脸检测
            ImageInfo imageInfo = getRGBData(new File(filename));
            List<FaceInfo> faceInfoList = new ArrayList<FaceInfo>();
            errorCode = faceEngine.detectFaces(imageInfo.getImageData(), imageInfo.getWidth(),
                    imageInfo.getHeight(), imageInfo.getImageFormat(), faceInfoList);
            if(faceInfoList.isEmpty()){
                return false;
            }

            //设置活体测试           age 3d gender 也不能删
            //设置RGB/IR活体阈值，若不设置     内部默认RGB：0.5, IR：0.7
            errorCode = faceEngine.setLivenessParam(0.5f, 0.7f);
            //人脸属性检测
            FunctionConfiguration configuration = new FunctionConfiguration();
            configuration.setSupportAge(true);
            configuration.setSupportFace3dAngle(true);
            //  性别 未知性别=-1 、男性=0 、女性=1
            configuration.setSupportGender(true);
            configuration.setSupportLiveness(true);
            errorCode = faceEngine.process(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(), imageInfo.getImageFormat(), faceInfoList, configuration);

            //活体检测
            // LivenessInfo是RGB活体检测信息
// 参数是 liveness RGB活体值，   未知=-1 、非活体=0 、活体=1、超出人脸=-2

            List<LivenessInfo> livenessInfoList = new ArrayList<LivenessInfo>();
            errorCode = faceEngine.getLiveness(livenessInfoList);
            System.out.println("RGB Liveness Detection：" + livenessInfoList.get(0).getLiveness());
            if(livenessInfoList.get(0).getLiveness()==1){
                return true;
            }else{
                return false;
            }
    }

    // 人脸相似度对比
    public boolean similarity(Integer eid, String filename){

        String appId = "CDHewBaWcAy2uz4Mn8raaWneBjC7C7wUYaotz2Enx3Hz";
        String sdkKey = "AQD5PgF41UwbfD59A1ncZfdBHtd1XCAV8MhNaASdNZ4F";
        FaceEngine faceEngine = new FaceEngine("D:\\0-YEAR4\\InividualProject\\arcsoft_lib");
        //激活引擎
        int errorCode = faceEngine.activeOnline(appId, sdkKey);
        if (errorCode != ErrorInfo.MOK.getValue() && errorCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()) {
            System.out.println("引擎激活失败");
        }

        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
        errorCode = faceEngine.getActiveFileInfo(activeFileInfo);
        if (errorCode != ErrorInfo.MOK.getValue() && errorCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()) {
            System.out.println("获取激活文件信息失败");
        }

        //引擎配置
        EngineConfiguration engineConfiguration = new EngineConfiguration();

        engineConfiguration.setDetectMode(DetectMode.ASF_DETECT_MODE_IMAGE);

        engineConfiguration.setDetectFaceOrientPriority(DetectOrient.ASF_OP_ALL_OUT);
        engineConfiguration.setDetectFaceMaxNum(10);
        engineConfiguration.setDetectFaceScaleVal(16);
        //功能配置
        FunctionConfiguration functionConfiguration = new FunctionConfiguration();
        functionConfiguration.setSupportAge(true);
        functionConfiguration.setSupportFace3dAngle(true);

        functionConfiguration.setSupportFaceDetect(true);

        functionConfiguration.setSupportFaceRecognition(true);
        functionConfiguration.setSupportGender(true);
        functionConfiguration.setSupportLiveness(true);
        functionConfiguration.setSupportIRLiveness(true);
        engineConfiguration.setFunctionConfiguration(functionConfiguration);

        //初始化引擎
        errorCode = faceEngine.init(engineConfiguration);
        if (errorCode != ErrorInfo.MOK.getValue()) {
            System.out.println("初始化引擎失败");
        }


        // detect face
        ImageInfo imageInfo2 = getRGBData(new File(filename));
        List<FaceInfo> faceInfoList2 = new ArrayList<FaceInfo>();
        errorCode = faceEngine.detectFaces(imageInfo2.getImageData(), imageInfo2.getWidth(),
                imageInfo2.getHeight(), imageInfo2.getImageFormat(), faceInfoList2);
        //extract face feature
        FaceFeature targetFaceFeature = new FaceFeature();
        errorCode = faceEngine.extractFaceFeature(imageInfo2.getImageData(), imageInfo2.getWidth(),
                imageInfo2.getHeight(), imageInfo2.getImageFormat(), faceInfoList2.get(0), targetFaceFeature);

        //特征比对
        // 根据 eid找到face entity
        //face 是数据库中对应员工的face entity 包括fid eid feature
        Face face = new Face();
        face = faceService.selectByEid(eid);
        //targetFaceFeature.setFeatureData(face.getFeature());
        FaceFeature sourceFaceFeature = new FaceFeature();
        //  传入的照片 人脸2
        sourceFaceFeature.setFeatureData(face.getFeature());
        FaceSimilar faceSimilar = new FaceSimilar();
        errorCode = faceEngine.compareFaceFeature(targetFaceFeature, sourceFaceFeature, faceSimilar);
        System.out.println("Similarity：" + faceSimilar.getScore());
        if(faceSimilar.getScore()>0.79){
            return true;
        }else{
            return false;
        }
    }


}