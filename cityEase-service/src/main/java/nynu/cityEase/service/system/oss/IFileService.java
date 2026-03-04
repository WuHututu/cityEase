package nynu.cityEase.service.system.oss;

import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
    /**
     * 上传文件并返回访问 URL
     */
    String upload(MultipartFile file);
}