package nynu.cityEase.service.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import nynu.cityEase.api.vo.pms.*;
import nynu.cityEase.api.vo.user.UserSimpleVO;
import nynu.cityEase.service.pms.repository.entity.RepairOrderDO;import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import nynu.cityEase.api.vo.pms.RepairOrderQueryReq;
import nynu.cityEase.api.vo.pms.RepairOrderVO;

import java.util.List;

public interface IPmsRepairOrderService extends IService<RepairOrderDO> {

    List<UserSimpleVO> listRepairHandlers();

    RepairOrderVO getRepairDetailForAdmin(Long orderId);


    /**
     * 业主提交报修工单
     * * @param req 报修请求参数
     */
    void submitRepair(RepairSubmitReq req);
    /**
     * 物业后台：派发工单给维修人员
     */
    void dispatchOrder(RepairDispatchReq req);

    /**
     * 维修人员：处理完成工单打卡
     */
    void completeOrder(RepairCompleteReq req);

    /**
     * 物业后台：分页查询报修工单列表
     */
    Page<RepairOrderVO> getRepairPage(RepairOrderQueryReq req);

    /**
     * 业主评价并结单
     */
    void evaluateOrder(RepairEvaluateReq req);

    /** App端：分页获取“我的报修工单” */
    Page<RepairOrderVO> getMyRepairPage(RepairMyOrderQueryReq req);

    /** App端：工单详情（带鉴权：必须是本人） */
    RepairOrderVO getMyRepairDetail(Long orderId);

    /** App端：取消工单（带鉴权：必须是本人；状态必须允许取消） */
    void cancelOrder(RepairCancelReq req);

    /** 维修人员：分页获取“派给我的工单” */
    Page<RepairOrderVO> getMyAssignedPage(RepairHandlerOrderQueryReq req);
}