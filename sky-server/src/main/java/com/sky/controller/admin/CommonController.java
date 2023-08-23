package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * @Author: dy
 * @Date: 2023/8/22 19:08
 * @Description: 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @RequestMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传: {}", file);

        //  获取原始文件名
        String originalFilename = file.getOriginalFilename();

        //  获取后缀名
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        //  构建新文件名
        String objectName = UUID.randomUUID().toString() + extension;

        //  文件请求路径
        try {
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败：{}", e.toString());
//            log.error("文件上传失败: { }", e);
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
