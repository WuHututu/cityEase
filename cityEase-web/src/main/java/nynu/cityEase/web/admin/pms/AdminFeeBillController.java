package nynu.cityEase.web.admin.pms;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.pms.*;
import nynu.cityEase.service.pms.fee.service.IFeeBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 物业费账单管理 Controller
 */
@Api(tags = "物业费账单管理")
@RestController
@RequestMapping("/admin/pms/fee")
public class AdminFeeBillController {

    @Autowired
    private IFeeBillService feeBillService;

    @PostMapping("/page")
    @ApiOperation("分页查询物业费账单")
    public ResVo<Page<FeeBillPageVO>> page(@RequestBody FeeBillPageReq req) {
        return ResVo.ok(feeBillService.page(req));
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("查看账单详情")
    public ResVo<FeeBillDetailVO> detail(@PathVariable Long id) {
        return ResVo.ok(feeBillService.detail(id));
    }

    @PostMapping("/save")
    @ApiOperation("新增账单")
    public ResVo<Boolean> save(@RequestBody FeeBillUpsertReq req) {
        feeBillService.save(req);
        return ResVo.ok(true);
    }

    @PostMapping("/update")
    @ApiOperation("修改账单")
    public ResVo<Boolean> update(@RequestBody FeeBillUpsertReq req) {
        feeBillService.update(req);
        return ResVo.ok(true);
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除账单")
    public ResVo<Boolean> delete(@PathVariable Long id) {
        feeBillService.delete(id);
        return ResVo.ok(true);
    }

    @PostMapping("/generate")
    @ApiOperation("批量生成账单（为所有房屋生成某月账单）")
    public ResVo<Integer> generate(@RequestBody FeeBillGenerateReq req) {
        int count = feeBillService.generate(req);
        return ResVo.ok(count);
    }

    @PostMapping("/markPaid")
    @ApiOperation("标记为已缴费")
    public ResVo<Boolean> markPaid(@RequestBody FeeBillMarkPaidReq req) {
        feeBillService.markPaid(req);
        return ResVo.ok(true);
    }

    @PostMapping("/markUnpaid/{id}")
    @ApiOperation("标记为未缴费")
    public ResVo<Boolean> markUnpaid(@PathVariable Long id) {
        feeBillService.markUnpaid(id);
        return ResVo.ok(true);
    }
}
