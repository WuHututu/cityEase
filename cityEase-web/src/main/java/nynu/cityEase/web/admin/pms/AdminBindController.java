package nynu.cityEase.web.admin.pms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.pms.BindAuditDetailVO;
import nynu.cityEase.api.vo.pms.BindAuditPageVO;
import nynu.cityEase.api.vo.pms.BindAuditPageReq;
import nynu.cityEase.service.pms.service.IBindAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/pms/bind")
@Api(tags="房产绑定审核管理")
public class AdminBindController {

    @Autowired
    private IBindAuditService bindAuditService;

    @PostMapping("/page")
    @ApiOperation("分页查询绑定申请")
    public ResVo<Page<BindAuditPageVO>> page(@RequestBody BindAuditPageReq req){
        return ResVo.ok(bindAuditService.getBindPage(req));
    }

    @PostMapping("/approve")
    @ApiOperation("审核通过")
    public ResVo<String> approve(@RequestBody java.util.Map<String,Object> body){
        Object idObj = body.get("id");
        if(idObj==null) return ResVo.fail(StatusEnum.valueOf("缺少申请ID"));
        bindAuditService.approve(Long.valueOf(String.valueOf(idObj)));
        return ResVo.ok("审核通过");
    }

    @PostMapping("/reject")
    @ApiOperation("审核拒绝")
    public ResVo<String> reject(@RequestBody java.util.Map<String,Object> body){
        Object idObj=body.get("id");
        if(idObj==null) return ResVo.fail(StatusEnum.valueOf("缺少申请ID"));
        String remark=String.valueOf(body.getOrDefault("remark",""));
        bindAuditService.reject(Long.valueOf(String.valueOf(idObj)),remark);
        return ResVo.ok("已拒绝");
    }

    @GetMapping("/detail")
    @ApiOperation("查看申请详情")
    public ResVo<BindAuditDetailVO> detail(@RequestParam("id")Long id){
        return ResVo.ok(bindAuditService.getBindDetail(id));
    }
}
