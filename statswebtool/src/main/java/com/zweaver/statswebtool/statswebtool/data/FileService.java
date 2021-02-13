package com.zweaver.statswebtool.statswebtool.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {
    @Value("${app.upload.dir:${user.home}}")
    public static String filePath;
    
    // 0 - file path
    // 1 - file name
    public static String[] returnItems = {"0", "0"};
    
    public static String[] uploadFile(MultipartFile file) {
        try {
            Path copyLocation = Paths.get(filePath + File.separator + StringUtils.cleanPath(file.getOriginalFilename()));
            Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);
            returnItems[0] = copyLocation.toString();
            returnItems[1] = copyLocation.getFileName().toString();
            return returnItems;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return returnItems;
    }
}
