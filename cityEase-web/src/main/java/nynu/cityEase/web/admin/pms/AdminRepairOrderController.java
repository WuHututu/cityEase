package nynu.cityEase.web.admin.pms;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.pms.RepairCompleteReq;
import nynu.cityEase.api.vo.pms.RepairDispatchReq;
import nynu.cityEase.api.vo.pms.RepairOrderQueryReq;
import nynu.cityEase.api.vo.pms.RepairOrderVO;
import nynu.cityEase.service.pms.service.IPmsRepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@RestController
@RequestMapping("/admin/pms/repair")
@Api(tags = "【后台】物业工单管理")
public class AdminRepairOrderController {

    @Autowired
    private IPmsRepairOrderService repairOrderService;

    @PostMapping("/dispatch")
    @ApiOperation("派发工单给维修员工")
    public ResVo<String> dispatchOrder(@RequestBody RepairDispatchReq req) {
        repairOrderService.dispatchOrder(req);
        return ResVo.ok("工单派发成功");
    }

    @PostMapping("/complete")
    @ApiOperation("维修员工提交处理结果")
    public ResVo<String> completeOrder(@RequestBody RepairCompleteReq req) {
        repairOrderService.completeOrder(req);
        return ResVo.ok("工单处理结果已提交");
    }

    @PostMapping("/page")
    @ApiOperation("分页获取工单列表")
    public ResVo<Page<RepairOrderVO>> getRepairPage(@RequestBody RepairOrderQueryReq req) {
        return ResVo.ok(repairOrderService.getRepairPage(req));
    }
}