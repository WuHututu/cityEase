package nynu.cityEase.service.system.oss.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.service.system.oss.IFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 阿里云 OSS 文件上传服务实现
 */
@Service("aliyunOssService")
public class AliyunOssServiceImpl implements IFileService {

    private static final Logger log = LoggerFactory.getLogger(AliyunOssServiceImpl.class);
    
    // 允许上传的图片格式
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp");
    // 最大文件大小：5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;
    
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    
    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;
    
    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;
    
    @Value("${aliyun.oss.file-host}")
    private String fileHost;

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
        
        boolean isAllowedType = ALLOWED_IMAGE_TYPES.stream()
                .anyMatch(type -> originalFilename.toLowerCase().endsWith(type));
        if (!isAllowedType) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, 
                    "不支持的图片格式，仅支持：jpg, jpeg, png, gif, bmp, webp");
        }

        OSS ossClient = null;
        try {
            // 3. 创建 OSSClient 实例
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            
            // 4. 生成文件路径（按日期分目录存储）
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd/"));
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = fileHost + datePath + UUID.randomUUID().toString().replace("-", "") + suffix;
            
            // 5. 上传文件到 OSS
            byte[] fileBytes = file.getBytes();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
            
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            
            // 6. 构造文件访问 URL
            // 格式：https://{bucket}.{endpoint}/{objectName}
            String url = "https://" + bucketName + "." + endpoint.replaceFirst("^https?://", "") + "/" + objectName;
            
            log.info("图片上传到阿里云 OSS 成功：{}", url);
            return url;
            
        } catch (IOException e) {
            log.error("图片上传失败", e);
            throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "图片上传失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
