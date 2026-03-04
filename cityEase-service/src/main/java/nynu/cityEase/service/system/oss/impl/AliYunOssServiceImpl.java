package nynu.cityEase.service.system.oss.impl;

import nynu.cityEase.service.system.oss.IFileService;
import nynu.cityEase.service.system.oss.OSSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Primary // 优先使用阿里云存储
public class AliYunOssServiceImpl implements IFileService {

    private static final Logger log = LoggerFactory.getLogger(AliYunOssServiceImpl.class);

    @Autowired
    private OSSUtils ossUtils;

    @Override
    public String upload(MultipartFile file) {
        log.info("开始使用阿里云 OSS 上传文件: {}", file.getOriginalFilename());
        // 直接调用你的 Utils 工具类完成上传
        String url = ossUtils.upload(file);
        log.info("阿里云 OSS 上传成功，访问地址: {}", url);
        // 返回以 https 开头的公网 URL
        return url;
    }
}