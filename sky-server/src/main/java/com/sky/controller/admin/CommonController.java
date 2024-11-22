package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "文件上传")
public class CommonController {

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result uploadFile(){
        return Result.success();
    }

}
