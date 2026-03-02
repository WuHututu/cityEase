package nynu.cityEase.web.front.pms;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.pms.RepairEvaluateReq;
import nynu.cityEase.api.vo.pms.RepairSubmitReq;
import nynu.cityEase.service.pms.service.IPmsRepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/pms/repair")
@Api(tags = "【App端】物业报修管理")
public class AppRepairOrderController {

    @Autowired
    private IPmsRepairOrderService repairOrderService;

    @PostMapping("/submit")
    @ApiOperation("提交报修工单")
    public ResVo<String> submitRepair(@RequestBody RepairSubmitReq req) {
        repairOrderService.submitRepair(req);
        return ResVo.ok("报修提交成功，物业将尽快为您处理");
    }

    @PostMapping("/evaluate")
    @ApiOperation("业主对处理完毕的工单进行评价结单")
    public ResVo<String> evaluateOrder(@RequestBody RepairEvaluateReq req) {
        repairOrderService.evaluateOrder(req);
        return ResVo.ok("评价成功！感谢您的反馈，已为您发放5个社区积分！");
    }
}