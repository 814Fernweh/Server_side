package com.example.project.config;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.UUID;

public class UploadUtils {

    //test filename is 3.jpg
    public static String upload(MultipartFile file, String path, String fileName) throws Exception {
        // Generate a new file name
        String realPath = path + "/" +fileName;
        File dest = new File(realPath);
        // Determine if the parent directory of a file exists
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdir();
        }
        // save the file
        file.transferTo(dest);
        return dest.getName();
    }
}