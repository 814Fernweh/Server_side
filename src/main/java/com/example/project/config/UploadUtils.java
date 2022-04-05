package com.example.project.config;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.UUID;

public class UploadUtils {

    // filename 为3.jpg
    public static String upload(MultipartFile file, String path, String fileName) throws Exception {
        // 生成新的文件名
        String realPath = path + "/" +fileName;
        File dest = new File(realPath);
        // 判断文件父目录是否存在
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdir();
        }
        // 保存文件
        file.transferTo(dest);
        return dest.getName();
    }
}