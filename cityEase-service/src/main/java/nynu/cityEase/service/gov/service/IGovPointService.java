package nynu.cityEase.service.gov.service;

import com.baomidou.mybatisplus.extension.service.IService;
import nynu.cityEase.service.gov.repository.entity.GovPointLogDO;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/3/1
 * Time: 14:30
 * Description: TODO
 */

public interface IGovPointService extends IService<GovPointLogDO>{

    /**
     * 核心方法：操作积分 (增/减)
     * @param roomId 房屋ID
     * @param userId 操作用户ID
     * @param amount 变动数量 (正数)
     * @param isAdd  true-增加积分，false-扣减积分
     * @param reason 变动原因
     */
    void changePoints(Long roomId, Long userId, Integer amount, boolean isAdd, String reason);
}