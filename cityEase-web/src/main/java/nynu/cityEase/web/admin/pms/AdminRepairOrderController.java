package nynu.cityEase.web.admin.pms;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.pms.*;
import nynu.cityEase.api.vo.user.UserSimpleVO;
import nynu.cityEase.service.pms.repository.entity.RepairOrderDO;
import nynu.cityEase.service.pms.service.IPmsRepairOrderService;
import nynu.cityEase.service.pms.service.impl.PmsRepairOrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

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

    @PostMapping("/detail")
    @ApiOperation("后台查看工单详情")
    public ResVo<RepairOrderVO> detail(@RequestBody RepairDetailReq req) {
        Long oid = req.getOrderId() != null ? req.getOrderId() : req.getId();
        if (oid == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "缺少工单ID");
        }
        return ResVo.ok(repairOrderService.getRepairDetailForAdmin(oid));
    }



    @PostMapping("/myAssigned/page")
    @ApiOperation("维修人员分页获取派给我的工单")
    public ResVo<Page<RepairOrderVO>> myAssigned(@RequestBody RepairHandlerOrderQueryReq req) {
        return ResVo.ok(repairOrderService.getMyAssignedPage(req));
    }

    @GetMapping("/handlers")
    @ApiOperation("获取可派单维修人员列表")
    public ResVo<List<UserSimpleVO>> handlers() {
        return ResVo.ok(repairOrderService.listRepairHandlers());
    }


}