package nynu.cityEase.service.system.oss.impl;

import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.service.system.oss.IFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class LocalFileServiceImpl implements IFileService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileServiceImpl.class);
    
    // 定义文件保存的绝对物理路径 (项目根目录下的 uploads 文件夹)
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
    // 定义文件对外的网络访问前缀
    private static final String URL_PREFIX = "http://localhost:8080/uploads/";

    @Override
    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "上传文件不能为空");
        }

        // 1. 确保目录存在
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 2. 生成全局唯一的文件名 (防重名覆盖)
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString().replace("-", "") + suffix;

        // 3. 将文件写入磁盘
        File destFile = new File(UPLOAD_DIR + newFilename);
        try {
            file.transferTo(destFile);
            log.info("文件上传成功: {}", destFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "文件上传失败");
        }

        // 4. 返回可直接访问的 URL 路径，这个路径会被存入数据库
        return URL_PREFIX + newFilename;
    }
}