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
    
    // 允许上传的图片格式
    private static final String[] ALLOWED_IMAGE_TYPES = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
    // 最大文件大小：5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    // 定义文件保存的绝对物理路径 (项目根目录下的 uploads/images 文件夹)
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "images" + File.separator;
    // 定义文件对外的网络访问前缀
    private static final String URL_PREFIX = "/uploads/images/";

    @Override
    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "上传文件不能为空");
        }
    
        // 1. 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, 
                    String.format("文件大小不能超过 %dMB", MAX_FILE_SIZE / 1024 / 1024));
        }
    
        // 2. 校验文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文件名不能为空");
        }
            
        boolean isAllowedType = false;
        for (String type : ALLOWED_IMAGE_TYPES) {
            if (originalFilename.toLowerCase().endsWith(type)) {
                isAllowedType = true;
                break;
            }
        }
        if (!isAllowedType) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, 
                    "不支持的图片格式，仅支持：jpg, jpeg, png, gif, bmp, webp");
        }
    
        // 3. 确保目录存在
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    
        // 4. 生成全局唯一的文件名 (防重名覆盖)
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString().replace("-", "") + suffix;
    
        // 5. 将文件写入磁盘
        File destFile = new File(UPLOAD_DIR + newFilename);
        try {
            file.transferTo(destFile);
            log.info("图片上传成功：{}", destFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("图片保存失败", e);
            throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "图片上传失败");
        }
    
        // 6. 返回相对路径（不包含域名），方便后续配置 CDN
        return URL_PREFIX + newFilename;
    }
}