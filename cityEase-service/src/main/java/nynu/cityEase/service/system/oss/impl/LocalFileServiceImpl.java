package nynu.cityEase.service.system.oss.impl;

import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.service.system.oss.IFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class LocalFileServiceImpl implements IFileService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileServiceImpl.class);
    private static final String[] ALLOWED_IMAGE_TYPES = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "images" + File.separator;
    private static final String URL_PREFIX = "/uploads/images/";

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

        boolean isAllowedType = false;
        for (String type : ALLOWED_IMAGE_TYPES) {
            if (originalFilename.toLowerCase().endsWith(type)) {
                isAllowedType = true;
                break;
            }
        }
        if (!isAllowedType) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED,
                    "不支持的图片格式，仅支持：jpg、jpeg、png、gif、bmp、webp");
        }

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString().replace("-", "") + suffix;
        File destFile = new File(UPLOAD_DIR + newFilename);
        try {
            file.transferTo(destFile);
            log.info("图片上传成功: {}", destFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("图片保存失败", e);
            throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "图片上传失败");
        }

        return URL_PREFIX + newFilename;
    }
}
