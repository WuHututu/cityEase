package nynu.cityEase.web.front.pms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.pms.*;
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

    // cityEase-web/src/main/java/nynu/cityEase/web/front/pms/AppRepairOrderController.java

    @PostMapping("/my/page")
    @ApiOperation("分页获取我的报修工单")
    public ResVo<Page<RepairOrderVO>> myPage(@RequestBody RepairMyOrderQueryReq req) {
        return ResVo.ok(repairOrderService.getMyRepairPage(req));
    }

    @PostMapping("/my/detail")
    @ApiOperation("获取我的报修工单详情")
    public ResVo<RepairOrderVO> myDetail(@RequestBody RepairDetailReq req) {
        return ResVo.ok(repairOrderService.getMyRepairDetail(req.getOrderId()));
    }

    @PostMapping("/cancel")
    @ApiOperation("取消我的报修工单")
    public ResVo<String> cancel(@RequestBody RepairCancelReq req) {
        repairOrderService.cancelOrder(req);
        return ResVo.ok("工单取消成功");
    }

}