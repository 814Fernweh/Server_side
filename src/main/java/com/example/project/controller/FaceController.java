package com.example.project.controller;
import com.example.project.entity.Face;
import com.example.project.service.FaceService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.arcsoft.face.*;
import com.arcsoft.face.enums.*;
import com.arcsoft.face.toolkit.ImageInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.arcsoft.face.toolkit.ImageInfoEx;

import static com.arcsoft.face.toolkit.ImageFactory.getGrayData;
import static com.arcsoft.face.toolkit.ImageFactory.getRGBData;

//  添加人脸信息
@Controller
@RequestMapping(value = "/face")//设置访问改控制类的"别名"
public class FaceController {
    @Resource
    private FaceService faceService;
    @ResponseBody
    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public Face add(){
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
        ImageInfo imageInfo = getRGBData(new File("D:\\0-YEAR4\\InividualProject\\project\\database-ZL01.jpg"));
        // d:\aaa.jpg zl证件照 不是活体      d:\ccc.jpg zl的是活体
        // C:\Users\zhuling\Desktop\zrm.jpg zrm 证件照 活体
        // C:\Users\zhuling\Desktop\wlm.jpg  wlm的 活体

        List<FaceInfo> faceInfoList = new ArrayList<FaceInfo>();
        errorCode = faceEngine.detectFaces(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(), imageInfo.getImageFormat(), faceInfoList);
        //特征提取 图片1
        FaceFeature faceFeature = new FaceFeature();
        errorCode = faceEngine.extractFaceFeature(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(), imageInfo.getImageFormat(), faceInfoList.get(0), faceFeature);
   //     System.out.println("特征值：" + faceFeature.getFeatureData().toString());
    //    System.out.println("特征值大小：" + faceFeature.getFeatureData().length);

        Face face= new Face();
        // 员工的工号 对应的人脸值 由公司存入数据库 不需要员工自己录入
        face.setFid(1);
        face.seteId(1);
        face.setFeature(faceFeature.getFeatureData());
        faceService.insert(face);
        System.out.print("成功插入人脸特征数据\n");
        return face;
    }
}





