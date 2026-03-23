package nynu.cityEase.service.system.oss.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
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

@Service("aliyunOssService")
public class AliyunOssServiceImpl implements IFileService {

    private static final Logger log = LoggerFactory.getLogger(AliyunOssServiceImpl.class);
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp");

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

    @Value("${upload.max-file-size-bytes:1048576}")
    private long maxFileSizeBytes;

    @Override
    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "上传文件不能为空");
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "上传文件不能超过 %d bytes", maxFileSizeBytes);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文件名不能为空");
        }

        boolean isAllowedType = ALLOWED_IMAGE_TYPES.stream()
                .anyMatch(type -> originalFilename.toLowerCase().endsWith(type));
        if (!isAllowedType) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED,
                    "不支持的图片格式，仅支持：jpg、jpeg、png、gif、bmp、webp");
        }

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd/"));
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = fileHost + datePath + UUID.randomUUID().toString().replace("-", "") + suffix;

            byte[] fileBytes = file.getBytes();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
            ossClient.putObject(new PutObjectRequest(bucketName, objectName, inputStream));

            String url = "https://" + bucketName + "." + endpoint.replaceFirst("^https?://", "") + "/" + objectName;
            log.info("图片上传到阿里云 OSS 成功: {}", url);
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
