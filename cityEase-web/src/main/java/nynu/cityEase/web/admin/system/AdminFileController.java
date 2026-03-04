package nynu.cityEase.web.admin.system;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.service.system.oss.IFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/file")
@Api(tags = "【后台】文件与OSS上传")
public class AdminFileController {

    @Autowired
    private IFileService fileService;

    @PostMapping("/upload")
    @ApiOperation("通用文件上传")
    public ResVo<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String url = fileService.upload(file);
        return ResVo.ok(url);
    }
}