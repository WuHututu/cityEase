package nynu.cityEase.web.admin.pms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.pms.*;
import nynu.cityEase.service.pms.fee.service.IFeeBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/pms/fee")
@Api(tags = "物业费账单管理")
public class AdminFeeBillController {

    @Autowired
    private IFeeBillService feeBillService;

    @PostMapping("/page")
    @ApiOperation("物业费账单分页查询")
    public ResVo<Page<FeeBillPageVO>> page(@RequestBody FeeBillPageReq req) {
        return ResVo.ok(feeBillService.page(req));
    }

    @GetMapping("/detail")
    @ApiOperation("物业费账单详情")
    public ResVo<FeeBillDetailVO> detail(@RequestParam("id") Long id) {
        return ResVo.ok(feeBillService.detail(id));
    }

    @PostMapping("/save")
    @ApiOperation("新增账单")
    public ResVo<String> save(@RequestBody FeeBillUpsertReq req) {
        feeBillService.save(req);
        return ResVo.ok("保存成功");
    }

    @PostMapping("/update")
    @ApiOperation("编辑账单")
    public ResVo<String> update(@RequestBody FeeBillUpsertReq req) {
        feeBillService.update(req);
        return ResVo.ok("更新成功");
    }

    @PostMapping("/delete")
    @ApiOperation("删除账单")
    public ResVo<String> delete(@RequestBody java.util.Map<String, Object> body) {
        Object idObj = body.get("id");
        if (idObj == null) return ResVo.fail(StatusEnum.valueOf("缺少账单ID"));
        feeBillService.delete(Long.valueOf(String.valueOf(idObj)));
        return ResVo.ok("删除成功");
    }

    @PostMapping("/generate")
    @ApiOperation("批量生成某月账单（已存在的不再生成）")
    public ResVo<String> generate(@RequestBody FeeBillGenerateReq req) {
        int created = feeBillService.generate(req);
        return ResVo.ok("已生成 " + created + " 条账单");
    }

    @PostMapping("/markPaid")
    @ApiOperation("标记已缴费")
    public ResVo<String> markPaid(@RequestBody FeeBillMarkPaidReq req) {
        feeBillService.markPaid(req);
        return ResVo.ok("已标记缴费");
    }

    @PostMapping("/markUnpaid")
    @ApiOperation("取消缴费（回到待缴费）")
    public ResVo<String> markUnpaid(@RequestBody java.util.Map<String, Object> body) {
        Object idObj = body.get("id");
        if (idObj == null) return ResVo.fail(StatusEnum.valueOf("缺少账单ID"));
        feeBillService.markUnpaid(Long.valueOf(String.valueOf(idObj)));
        return ResVo.ok("已取消缴费");
    }
}
