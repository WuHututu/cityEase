package nynu.cityEase.service.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import nynu.cityEase.api.vo.pms.FeeBillVO;
import nynu.cityEase.service.pms.repository.entity.FeeBillDO;

import java.util.List;

public interface IPmsFeeBillService extends IService<FeeBillDO> {
    
    /**
     * 核心动作：定时任务批量生成某月的物业费账单
     */
    void generateMonthlyBills(String targetMonth);

    /**
     * 核心动作：业主发起支付
     */
    void payBill(Long billId);

    /**
     * 业主端：获取当前登录用户的所有房屋账单
     */
    List<FeeBillVO> getMyBills();
}