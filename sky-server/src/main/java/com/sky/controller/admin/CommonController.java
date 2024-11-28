package com.sky.controller.admin;

import com.sky.result.Result;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import io.minio.http.Method;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "文件上传")
@Slf4j
public class CommonController {

    @Autowired
    private MinioClient minioClient;

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        
        String fileName = file.getOriginalFilename();

        //存储文件
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("skytakeout")
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        // 生成文件的访问 URL
//        // 生成文件的预签名 URL
//        String fileUrl = minioClient.getPresignedObjectUrl(
//                GetPresignedObjectUrlArgs.builder()
//                        .bucket("skytakeout")
//                        .object(fileName)
//                        .method(Method.GET)
//                        .expiry(24, TimeUnit.HOURS) // 设置 URL 的有效期为 24 小时
//                        .build()
//        );
        String fileUrl = "http://192.168.223.141:9000/skytakeout/" + fileName;
        log.info("上传成功：{}", fileName);
        log.info("{}",fileUrl);
        return Result.success(fileUrl);
    }

}
