package com.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "http://localhost:8081")
public class FileUploadController {

    // ✅ 统一使用项目根目录下的 images 文件夹（外部目录，非 src/）
    private static final String UPLOAD_DIR = "images/";

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
            String fileName = UUID.randomUUID().toString() + ext;

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path targetPath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // ✅ 返回数据库存储的路径（与默认图一致）
            String url = "/images/" + fileName;
            return ResponseEntity.ok(Map.of("url", url, "message", "上传成功"));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "上传失败: " + e.getMessage()));
        }
    }
}