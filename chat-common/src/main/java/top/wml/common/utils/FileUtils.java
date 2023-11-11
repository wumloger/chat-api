package top.wml.common.utils;

import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {

    /**
     * 编码文件名
     * 日期路径 + UUID
     * 示例：fileName=2022/11/18/统计报表1668758006562.txt
     */
    public static final String extractUploadFilename(MultipartFile file)
    {
        String fileName = file.getOriginalFilename();
        // 注意，这里需要加上 \\ 将 特殊字符 . 转意 \\. ,否则异常
        String[] fileArray = fileName.split("\\.");
        fileName = datePath() + "/" + fileArray[0]+System.currentTimeMillis()+"."+fileArray[1];
        return fileName;
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static final String datePath() {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        return format.format(now);
    }
}
