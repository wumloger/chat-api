package top.wml.user.controller;

import cn.hutool.json.JSONObject;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.wml.common.annotation.TokenRequired;
import top.wml.common.resp.CommonResp;
import top.wml.common.utils.JwtUtil;
import top.wml.user.minio.MinioConfig;
import top.wml.user.minio.MinioUtils;

import java.io.InputStream;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/oss")
public class OssController {

    @Resource
    private MinioUtils minioUtils;

    @Autowired
    private MinioConfig minioConfig;

    private HttpServletRequest request;
    /**
     * 文件上传
     *
     * @param file
     */
    @PostMapping("/upload")
    @TokenRequired
    public CommonResp upload(@RequestParam("file") MultipartFile file) {
        CommonResp<String> resp = new CommonResp<>();
        try {
            //文件名
            String fileName = file.getOriginalFilename();
            int i = fileName.lastIndexOf(".");
            if (i == -1) {
                resp.fail("文件格式错误");
            }
            String suffix = fileName.substring(i + 1);
//            String newFileName = System.currentTimeMillis() + "." + StringUtils.substringAfterLast(fileName, ".");
            String newFileName = System.currentTimeMillis() + "." + suffix;
            //获取当前年月日
            LocalDate localDate = LocalDate.now();
            int year = localDate.getYear();
            int month = localDate.getMonthValue();
            int day = localDate.getDayOfMonth();

            //获取用户id
            String token = request.getHeader("token");
            JSONObject jsonObject = JwtUtil.getJSONObject(token);
            Long id = jsonObject.get("id", Long.class);
            //按id和时间分文件
            newFileName = id + "/" + year + "/" + month + "/" + day + "/" + newFileName;
            //类型
            String contentType = file.getContentType();
            minioUtils.uploadFile(minioConfig.getBucketName(), file, newFileName, contentType);
            String url = minioUtils.getPresignedObjectUrl(minioConfig.getBucketName(), newFileName);
            System.out.println(url);
            resp.setMsg("上传成功");
            resp.success(url);
            return resp;
        } catch (Exception e) {
            log.error("上传失败", e);
            resp.fail("上传失败");
            return resp;
        }
    }

    /**
     * 删除
     *
     * @param fileName
     */
    @DeleteMapping()
    public void delete(@RequestParam("fileName") String fileName) {
        minioUtils.removeFile(minioConfig.getBucketName(), fileName);
    }

    /**
     * 获取文件信息
     *
     * @param fileName
     * @return
     */
    @GetMapping("/info")
    public String getFileStatusInfo(@RequestParam("fileName") String fileName) {
        return minioUtils.getFileStatusInfo(minioConfig.getBucketName(), fileName);
    }

    /**
     * 获取文件外链
     *
     * @param fileName
     * @return
     */
    @GetMapping("/url")
    public String getPresignedObjectUrl(@RequestParam("fileName") String fileName) {
        return minioUtils.getPresignedObjectUrl(minioConfig.getBucketName(), fileName);
    }

    /**
     * 文件下载
     *
     * @param fileName
     * @param response
     */
    @GetMapping("/download")
    public void download(@RequestParam("fileName") String fileName, HttpServletResponse response) {
        try {
            InputStream fileInputStream = minioUtils.getObject(minioConfig.getBucketName(), fileName);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setContentType("application/force-download");
            response.setCharacterEncoding("UTF-8");
            IOUtils.copy(fileInputStream, response.getOutputStream());
        } catch (Exception e) {
            log.error("下载失败");
        }
    }

}