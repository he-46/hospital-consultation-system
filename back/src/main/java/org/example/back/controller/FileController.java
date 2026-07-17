package org.example.back.controller;

import org.example.back.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 上传头像
     */
    @PostMapping("/uploadAvatar")
    public Result<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }
        
        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.error("文件名无效");
        }
        
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!suffix.matches("\\.(jpg|jpeg|png|gif)$")) {
            return Result.error("只支持jpg、jpeg、png、gif格式的图片");
        }
        
        // 检查文件大小（最大2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.error("图片大小不能超过2MB");
        }
        
        try {
            // 获取项目根目录
            String projectPath = System.getProperty("user.dir");
            String uploadDir = projectPath + File.separator + uploadPath.replace("./", "") + File.separator;
            
            // 创建上传目录
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 生成新文件名
            String newFileName = UUID.randomUUID().toString() + suffix;
            File destFile = new File(uploadDir + newFileName);
            
            // 保存文件
            file.transferTo(destFile);
            
            // 返回访问路径（通过 /api/file/uploads/xxx 访问）
            String fileUrl = "/uploads/" + newFileName;
            
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", newFileName);
            
            return Result.success(result);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败");
        }
    }
    
    /**
     * 获取文件（通过 Controller 提供静态文件访问）
     */
    @GetMapping("/uploads/**")
    public ResponseEntity<Resource> getFile(HttpServletRequest request) {
        try {
            String uri = request.getRequestURI();
            String filename = uri.substring(uri.lastIndexOf("/uploads/") + "/uploads/".length());
            
            String projectPath = System.getProperty("user.dir");
            String filePath = projectPath + File.separator + uploadPath.replace("./", "") + File.separator + filename;
            
            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}
