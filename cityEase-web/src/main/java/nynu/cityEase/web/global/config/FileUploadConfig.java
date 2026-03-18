package nynu.cityEase.web.global.config;

import nynu.cityEase.service.system.oss.IFileService;
import nynu.cityEase.service.system.oss.impl.AliyunOssServiceImpl;
import nynu.cityEase.service.system.oss.impl.LocalFileServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 文件上传服务配置
 */
@Configuration
public class FileUploadConfig {

    @Value("${upload.type:local}")
    private String uploadType;

    /**
     * 根据配置返回对应的文件上传服务实现
     * upload.type=aliyun 使用阿里云 OSS
     * upload.type=local 使用本地存储（默认）
     */
    @Bean
    @Primary
    public IFileService fileService() {
        if ("aliyun".equalsIgnoreCase(uploadType)) {
            return new AliyunOssServiceImpl();
        } else {
            return new LocalFileServiceImpl();
        }
    }
}
