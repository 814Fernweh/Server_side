package com.example.project.controller;

import com.arcsoft.face.*;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.DetectOrient;
import com.arcsoft.face.enums.ErrorInfo;
import com.arcsoft.face.toolkit.ImageInfo;
import com.example.project.config.UploadUtils;
import com.example.project.entity.Employee;
import com.example.project.entity.Face;
import com.example.project.service.EmployeeService;
import com.example.project.service.FaceService;
import com.example.project.service.RecordService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.arcsoft.face.toolkit.ImageFactory.getRGBData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Controller
@Transactional
@RequestMapping(value = "/addEmp")//设置访问改控制类的"别名"
public class AddEmployee {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private EmployeeService employeeService;
    @Resource
    private FaceService faceService;

    // 启动程序的初始页面 判断是否有管理员登录
    @RequestMapping(value = "/add_init")
    @ResponseBody
    public void search(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ((HttpServletResponse) response).sendRedirect("AddEmployee.html");
    }

    @RequestMapping(value="/addE",method = {RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> add(Integer no, String tele, String pwd, String ename,
                              Integer eage, BigDecimal longi, BigDecimal latit, String dep, String sex) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        Employee e=new Employee();
        e.setEid(no);
        e.setTelephone(tele);
        e.setPwd(pwd);
        e.setAge(eage);
        e.setName(ename);
        if(sex.equals("male")){
            e.setGender(0);
        }
        if(sex.equals("female")){
            e.setGender(1);
        }
        if(dep.equals("1")){
            e.setdId(1);
        }
        if(dep.equals("2")){
            e.setdId(2);
        }
        if(dep.equals("3")){
            e.setdId(3);
        }

        e.setLatitude(latit);
        e.setLongitude(longi);
        employeeService.insert(e);
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("code", 0);
        map.put("msg", "添加成功");
        map.put("data", e);
        logger.info("administrator add a new employee of ID "+ no);
        return map;
    }


    //图片上传并提取特征值 加入数据库  接口   https://www.jianshu.com/p/d0eb7c62e011 参考这个
    @PostMapping("/uploadImage")
    @ResponseBody
    public Map<String,Object> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String,Object> map  = new HashMap<>();
        // 图片保存位置
        String uploadDir = "D:/0-YEAR4/InividualProject/project/src/main/resources/faceimage/";
        try {
            // 图片路径
            String imgUrl = null;
            String a=file.getOriginalFilename();   // 原始名字
            //上传  UploadUtils 自定义的 在config里面
            String filename = UploadUtils.upload(file, uploadDir, file.getOriginalFilename());
            if (filename != null) {
                imgUrl = new File(uploadDir).getName() + "/" + file.getName();
                // 获取人脸特征值  存入数据库
                String appId = "CDHewBaWcAy2uz4Mn8raaWneBjC7C7wUYaotz2Enx3Hz";
                String sdkKey = "AQD5PgF41UwbfD59A1ncZfdBHtd1XCAV8MhNaASdNZ4F";
                FaceEngine faceEngine = new FaceEngine("D:\\0-YEAR4\\InividualProject\\arcsoft_lib");
                //激活引擎
                int errorCode = faceEngine.activeOnline(appId, sdkKey);
                if (errorCode != ErrorInfo.MOK.getValue() && errorCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()) {
                    System.out.println("引擎激活失败");
                }

                ActiveFileInfo activeFileInfo=new ActiveFileInfo();
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
                //人脸检测 图片1 检测目标
                ImageInfo imageInfo = getRGBData(new File(uploadDir+ "/" +file.getOriginalFilename()));
                List<FaceInfo> faceInfoList = new ArrayList<FaceInfo>();
                errorCode = faceEngine.detectFaces(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(), imageInfo.getImageFormat(), faceInfoList);
                //特征提取 图片1
                FaceFeature faceFeature = new FaceFeature();
                errorCode = faceEngine.extractFaceFeature(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(), imageInfo.getImageFormat(), faceInfoList.get(0), faceFeature);
                //     System.out.println("特征值：" + faceFeature.getFeatureData().toString());
                //    System.out.println("特征值大小：" + faceFeature.getFeatureData().length);

                Face face= new Face();
                // 员工的工号 对应的人脸值 由公司存入数据库 不需要员工自己录入
            //    face.setFid(1);
                Integer end=file.getOriginalFilename().lastIndexOf(".");
                Integer eid= Integer.valueOf(file.getOriginalFilename().substring(0,end));
                face.seteId(eid);
                face.setFeature(faceFeature.getFeatureData());
                faceService.insert(face);
                System.out.print("成功插入人脸\n");

            }
            map.put("code",0);
            map.put("msg","success upload");
            map.put("data",imgUrl);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            map.put("code",500);
            map.put("msg","fail");
            map.put("data",null);
            return map;
        }
    }

}
