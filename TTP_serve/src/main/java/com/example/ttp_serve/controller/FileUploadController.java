package com.example.ttp_serve.controller;

import com.example.ttp_serve.dto.MyApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@Tag(name = "文件上传", description = "文件上传相关接口")
@CrossOrigin(origins = "*")
public class FileUploadController {

    @Value("${upload.path:uploads/}")
    private String uploadPath;

    @Value("${upload.avatar.dir:avatars/}")
    private String avatarDir;

    @PostMapping("/avatar")
    @Operation(summary = "上传头像", description = "上传用户头像图片")
    public ResponseEntity<MyApiResponse<String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(MyApiResponse.error(400, "文件不能为空"));
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(MyApiResponse.error(400, "只能上传图片文件"));
            }

            // 检查文件大小 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(MyApiResponse.error(400, "文件大小不能超过5MB"));
            }

            // 创建上传目录
            String avatarPath = uploadPath + avatarDir;
            File uploadDir = new File(avatarPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + fileExtension;

            // 保存文件
            Path filePath = Paths.get(avatarPath + filename);
            Files.write(filePath, file.getBytes());

            // 返回文件访问URL
            String fileUrl = "/uploads/avatars/" + filename;
            
            return ResponseEntity.ok(MyApiResponse.success("头像上传成功", fileUrl));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(MyApiResponse.error(500, "文件上传失败: " + e.getMessage()));
        }
    }
}