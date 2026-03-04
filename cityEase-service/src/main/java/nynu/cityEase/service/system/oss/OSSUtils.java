package nynu.cityEase.service.system.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Component
public class OSSUtils {

    @Value("${aliyun.oss.endpoint:}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name:}")
    private String bucketName;

    @Value("${aliyun.oss.file-host:}")
    private String fileHost;

    /**
     * 上传文件到OSS
     * @param file 待上传的文件
     * @return 上传后的文件URL
     */
    public String upload(MultipartFile file) {
        // 1. 生成文件名（避免重复）：UUID + 原文件名
        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString().replaceAll("-", "") 
                + originalFilename.substring(originalFilename.lastIndexOf("."));

        // 2. 按日期组织文件路径（如：upload/20231117/xxx.jpg）
        String datePath = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String key = fileHost + datePath + "/" + fileName;

        // 3. 上传文件到OSS
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try (InputStream inputStream = file.getInputStream()) {
            // 设置文件元数据（可选，如ContentType）
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(getContentType(originalFilename.substring(originalFilename.lastIndexOf("."))));

            ossClient.putObject(bucketName, key, inputStream, metadata);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        } finally {
            ossClient.shutdown(); // 关闭客户端
        }

        // 4. 返回文件访问URL（public读写的Bucket可用，私有Bucket需生成签名URL）
        return "https://" + bucketName + "." + endpoint + "/" + key;
    }

    /**
     * 根据文件后缀获取ContentType
     */
    private String getContentType(String fileSuffix) {
        if (".jpg".equalsIgnoreCase(fileSuffix) || ".jpeg".equalsIgnoreCase(fileSuffix)) {
            return "image/jpeg";
        }
        if (".png".equalsIgnoreCase(fileSuffix)) {
            return "image/png";
        }
        if (".gif".equalsIgnoreCase(fileSuffix)) {
            return "image/gif";
        }
        return "application/octet-stream";
    }

    /**
     * 删除OSS上的文件
     * @param fileUrl 文件URL（如：<a href="https://bucket.oss-cn-beijing.aliyuncs.com/upload/xxx.jpg">...</a>）
     */
    public void deleteFile(String fileUrl) {
        // 修正key提取逻辑
        String prefix = "https://" + bucketName + "." + endpoint + "/";
        if (fileUrl.startsWith(prefix)) {
            String key = fileUrl.substring(prefix.length());
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            ossClient.deleteObject(bucketName, key);
            ossClient.shutdown();
        } else {
            throw new RuntimeException("无效的文件URL格式");
        }
    }

    /**
     * 从OSS下载文件内容
     * @param key 文件在OSS中的key
     * @return OSSObject对象，使用后需要关闭
     */
//    public com.aliyun.oss.model.OSSObject downloadFile(String key) {
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//        return ossClient.getObject(bucketName, key);
//    }

    /**
     * 获取文件访问URL
     * @param key 文件在OSS中的key
     * @return 文件访问URL
     */
    public String getFileUrl(String key) {
        return "https://" + bucketName + "." + endpoint + "/" + key;
    }
}