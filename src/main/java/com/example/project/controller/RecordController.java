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
import com.example.project.entity.Regulation;
import com.example.project.service.EmployeeService;
import com.example.project.service.FaceService;
import com.example.project.service.RecordService;
import com.example.project.service.RegulationService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.arcsoft.face.toolkit.ImageFactory.getRGBData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@Transactional
@RequestMapping(value = "/attendance")
public class RecordController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private FaceService faceService;
    @Resource
    private EmployeeService employeeService;
    @Resource
    private RecordService recordService;
    @Resource
    private RegulationService regulationService;

    //reference: https://www.it610.com/article/1293097618573959168.htm
    private static double PI = 3.14159265;
    private static double EARTH_RADIUS = 6378137;
    private static double RAD = Math.PI / 180.0;
    double clientLati,clientLong;
    Integer clienteID,clientType;
    Integer weekclienteID,flag;
    String radius="500";

    // change standard location range
    @RequestMapping(value="/changeRange",method = {RequestMethod.POST})
    @ResponseBody
    public  Map<String, Object>  changeRange(String dep) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        if(dep.equals("1")){
            radius="300";
        }
        if(dep.equals("2")){
            radius="500";
        }
        if(dep.equals("3")){
            radius="800";
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("code", 0);
        map.put("msg", "change ok");
        map.put("data", radius);
        logger.info("administrator change the standard location range to "+ radius);
        return map;
    }

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
            // process data
            byte[] bytes = new BASE64Decoder().decodeBuffer(initData.get("jpegData").toString());
            Date checkinDate=new Date();
            SimpleDateFormat format=new SimpleDateFormat("yyyyMMddHHmmss");
            jpegFilename="D:\\0-YEAR4\\InividualProject\\project\\"+clientTele+format.format(checkinDate)+".jpg";
            outputStream = new FileOutputStream(jpegFilename);
            // Writing in incoming images from the client
            outputStream.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (outputStream != null) {
                try {
                    //Closing the outputStream stream
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        int recordResult=addRecord(clientType,clienteID,clientLong,clientLati,jpegFilename,radius);
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
     * Get the date of the last few days
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


    // client view the records in the past 7 days
    // Android attendance data can be queried by week by default
    //Days without attendance records Only dates are shown
    @RequestMapping(value="/clientSearchWeekData",method = {RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> getWeek(String SearchWeekData) throws IOException {

        JSONObject weekdata=JSONObject.parseObject(SearchWeekData);
        weekclienteID=weekdata.getInteger("eID");
        flag=weekdata.getInteger("flag");
        ArrayList<Record> weekRecord;
        Record r;

        // Get the date of the previous 7 days
        ArrayList<String> pastDaysList = new ArrayList<>();
        try {
            Date today = new Date(); // today
            DateFormat day = DateFormat.getDateInstance() ;
            //The time passed here is a string type,
            // so it has to be converted to a date type first
            SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
            Date date =sdf.parse(day.format(today));
            for (int i = 6; i >= 0; i--) {
                pastDaysList.add(getPastDate(i,date));
            }
        }catch (ParseException e){
            e.printStackTrace();
        }
        // Gets the day of the week in String format
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i=0;i<7;i++){
            //Get the record for each date
            r=recordService.selectTheArrive(weekclienteID,pastDaysList.get(i));
            // Find the record, then send the attendance details to the client
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
    // client view the records in the past 31 days
    @RequestMapping(value="/clientSearchMonthData",method = {RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> getMonth(String SearchMonthData) throws IOException {

        JSONObject weekdata=JSONObject.parseObject(SearchMonthData);
        weekclienteID=weekdata.getInteger("eID");
        flag=weekdata.getInteger("flag");
        ArrayList<Record> weekRecord;
        Record r;

        ArrayList<String> pastDaysList = new ArrayList<>();
        try {
            Date today = new Date();
            DateFormat day = DateFormat.getDateInstance() ;
            SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
            Date date =sdf.parse(day.format(today));
            for (int i = 30; i >= 0; i--) {
                pastDaysList.add(getPastDate(i,date));
            }
        }catch (ParseException e){
            e.printStackTrace();
        }
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i=0;i<31;i++){

            r=recordService.selectTheArrive(weekclienteID,pastDaysList.get(i));
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



    /// Distance (in metres) calculated from the two sets of latitude and longitude provided
    // call it in isInCircle
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


    // Determining whether a point is within a circular area
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

    // add attendance records
    public int addRecord(Integer type,Integer eid,double longitude,double latitude,String filename,String radius) throws ParseException {
        // check whether the attendance is succesfully
        int ca=checkAttendance(eid,longitude,latitude,filename,radius);
        if(ca!=0){
            return ca;
        }
        Employee e;
        // if successful
        if(ca==0){
            // Check if this attendance record is available
                Record re;
                SimpleDateFormat df00 = new SimpleDateFormat("yyyy-MM-dd");//Set date format
                String s00=df00.format(new Date());
                System.out.println (s00) ;

                re=recordService.selectTheArrive(eid, s00); // Check if this attendance record is available

            // Get system time
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            String now=hour+":"+minute;
            // set the standard time
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
            // Compare the current time with the specified time for commuting to work
            int resultA=c1.compareTo(c2);
            int resultL=c1.compareTo(c3);
            e = employeeService.selectByPrimaryKey(eid);  //find the employee
            // No attendance record exists, this is the first time I have clocked in.
                if(re==null){
                    Record record = new Record();
                    record.seteId(eid);
                    record.setdId(e.getdId());
                    record.setName(e.getName());
                    SimpleDateFormat df0 = new SimpleDateFormat("yyyy-MM-dd");
                    String s=df0.format(new Date());
                    System.out.println (s) ;
                    record.setCheckinDate(s);

                    if(resultA==0 || resultA<0 ) {
                        // c1 is equal to c2 c1 is less than c2 means: Time earlier than office hours
                        DateFormat dateFormatterChina = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM);//格式化输出
                        TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai");
                        dateFormatterChina.setTimeZone(timeZoneChina);
                        Date curDate = new Date();

                        record.setArriveTime(curDate);
                        record.setAResult("normal");
                        record.setResult("0");    // Normal commute to work, but no off-duty record yet
                        recordService.insert(record);
                        System.out.print("1.Successful insertion of new work data\n");
                        return 0;
                    }

                    if(resultL==0 || resultL>0 ) {
                        // c1 is equal to c3 c1 is greater than c3
                        //Off duty normal On duty not clocked in!
                        record.setLeaveTime(new Date());   // add off work time
                        record.setLResult("normal");
                        record.setResult("0");
                        recordService.insert(record);
                        System.out.print("2.Successful insertion of new work data\n");
                        return 0;
                    }
                    if(resultA>0 || resultL<0 ) {
                        // late arrival
                        record.setArriveTime(new Date());
                        record.setAResult("late arrival");
                        record.setResult("0");
                        recordService.insert(record);
                        System.out.print("3.Successful insertion of new work data\n");
                        return 1;
                    }
                }

                // I've clocked in today and have an attendance record
                else{
                    Record updateRe;
                    updateRe=recordService.selectTheArrive(eid, s00);

                    Date date2 = new Date();
                    if(resultA==0 || resultA<0 ) {
                        // Earlier than on work time
                        // no update
                        return 3;

                    }
                    if(resultL==0 || resultL>0 ) {
                        //  late than off work time
                        //Normal work day, and clocked in at work  update the record

                        updateRe.setLeaveTime(date2);
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
                        // Leaving work early, clocking in and out  update record
                        updateRe.setLeaveTime(date2);
                        updateRe.setLResult("early departure");
                        updateRe.setResult("0");
                        recordService.updateByPrimaryKey(updateRe);
                        return 2;

                    }
                }
        }

        return -4;      //Other errors, Attendance failed, Prompt to retake attendance

    }

    // The client passes in the id of the employee, the geographic location, the photo of the face and compares it with the data stored in the database.
    public int checkAttendance(Integer eid, double longitude, double latitude, String filename,String radius) {

        // First determine if the location is right
        Employee employee;
        employee = employeeService.selectByPrimaryKey(eid);

        // Retrieve standard geographic location values from the database
        double stan_longitude = employee.getLongitude().doubleValue();
        double stan_latitude = employee.getLatitude().doubleValue();

        boolean withinLocation = isInCircle(stan_longitude, stan_latitude, longitude, latitude, radius);
        if (!withinLocation)
        {
            return -1;
        }
        // Position is correct, perform in vivo testing to determine if it's a live operation
        boolean isAlive=checkLiveness(filename);
        if(!isAlive)
        {
            return -2;   // no alive
        }
        // It's a live body, Continue with face similarity comparison
        boolean isSamePerson=similarity(eid,filename);
        if(!isSamePerson)
        {
            return -3;   // Lack of face similarity
        }
        return 0;

    }
        //liveness dtection
    public boolean checkLiveness(String filename){
            String appId = "CDHewBaWcAy2uz4Mn8raaWneBjC7C7wUYaotz2Enx3Hz";
            String sdkKey = "AQD5PgF41UwbfD59A1ncZfdBHtd1XCAV8MhNaASdNZ4F";
            FaceEngine faceEngine = new FaceEngine("D:\\0-YEAR4\\InividualProject\\arcsoft_lib");

            int errorCode = faceEngine.activeOnline(appId, sdkKey);
            if (errorCode != ErrorInfo.MOK.getValue() && errorCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()) {
                System.out.println("Engine activation failure");
            }

            ActiveFileInfo activeFileInfo = new ActiveFileInfo();
            errorCode = faceEngine.getActiveFileInfo(activeFileInfo);
            if (errorCode != ErrorInfo.MOK.getValue() && errorCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()) {
                System.out.println("Failed to get activation file information");
            }

            EngineConfiguration engineConfiguration = new EngineConfiguration();
            engineConfiguration.setDetectMode(DetectMode.ASF_DETECT_MODE_IMAGE);
            engineConfiguration.setDetectFaceOrientPriority(DetectOrient.ASF_OP_ALL_OUT);
            engineConfiguration.setDetectFaceMaxNum(10);
            engineConfiguration.setDetectFaceScaleVal(16);

            FunctionConfiguration functionConfiguration = new FunctionConfiguration();
            functionConfiguration.setSupportAge(true);
            functionConfiguration.setSupportFace3dAngle(true);
            functionConfiguration.setSupportFaceDetect(true);
            functionConfiguration.setSupportFaceRecognition(true);
            functionConfiguration.setSupportGender(true);

            functionConfiguration.setSupportLiveness(true);

            functionConfiguration.setSupportIRLiveness(true);
            engineConfiguration.setFunctionConfiguration(functionConfiguration);


            errorCode = faceEngine.init(engineConfiguration);
            if (errorCode != ErrorInfo.MOK.getValue()) {
                System.out.println("Failed to initialise the engine");
            }
            //detect face
            ImageInfo imageInfo = getRGBData(new File(filename));
            List<FaceInfo> faceInfoList = new ArrayList<FaceInfo>();
            errorCode = faceEngine.detectFaces(imageInfo.getImageData(), imageInfo.getWidth(),
                    imageInfo.getHeight(), imageInfo.getImageFormat(), faceInfoList);
            if(faceInfoList.isEmpty()){
                return false;
            }


            errorCode = faceEngine.setLivenessParam(0.5f, 0.7f);
            //Face attribute detection
            FunctionConfiguration configuration = new FunctionConfiguration();
            configuration.setSupportAge(true);
            configuration.setSupportFace3dAngle(true);

            configuration.setSupportGender(true);
            configuration.setSupportLiveness(true);
            errorCode = faceEngine.process(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(), imageInfo.getImageFormat(), faceInfoList, configuration);

            List<LivenessInfo> livenessInfoList = new ArrayList<LivenessInfo>();
            errorCode = faceEngine.getLiveness(livenessInfoList);
            System.out.println("RGB Liveness Detection：" + livenessInfoList.get(0).getLiveness());
            if(livenessInfoList.get(0).getLiveness()==1){
                return true;
            }else{
                return false;
            }
    }

    // face similarity
    public boolean similarity(Integer eid, String filename){

        String appId = "CDHewBaWcAy2uz4Mn8raaWneBjC7C7wUYaotz2Enx3Hz";
        String sdkKey = "AQD5PgF41UwbfD59A1ncZfdBHtd1XCAV8MhNaASdNZ4F";
        FaceEngine faceEngine = new FaceEngine("D:\\0-YEAR4\\InividualProject\\arcsoft_lib");

        int errorCode = faceEngine.activeOnline(appId, sdkKey);
        if (errorCode != ErrorInfo.MOK.getValue() && errorCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()) {
            System.out.println("Engine activation failure");
        }

        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
        errorCode = faceEngine.getActiveFileInfo(activeFileInfo);
        if (errorCode != ErrorInfo.MOK.getValue() && errorCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()) {
            System.out.println("Failed to get activation file information");
        }


        EngineConfiguration engineConfiguration = new EngineConfiguration();

        engineConfiguration.setDetectMode(DetectMode.ASF_DETECT_MODE_IMAGE);
        engineConfiguration.setDetectFaceOrientPriority(DetectOrient.ASF_OP_ALL_OUT);
        engineConfiguration.setDetectFaceMaxNum(10);
        engineConfiguration.setDetectFaceScaleVal(16);

        FunctionConfiguration functionConfiguration = new FunctionConfiguration();
        functionConfiguration.setSupportAge(true);
        functionConfiguration.setSupportFace3dAngle(true);
        functionConfiguration.setSupportFaceDetect(true);
        functionConfiguration.setSupportFaceRecognition(true);
        functionConfiguration.setSupportGender(true);
        functionConfiguration.setSupportLiveness(true);
        functionConfiguration.setSupportIRLiveness(true);
        engineConfiguration.setFunctionConfiguration(functionConfiguration);


        errorCode = faceEngine.init(engineConfiguration);
        if (errorCode != ErrorInfo.MOK.getValue()) {
            System.out.println("Failed to initialise the engine");
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


        Face face = new Face();
        face = faceService.selectByEid(eid);
        FaceFeature sourceFaceFeature = new FaceFeature();

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