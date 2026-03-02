package nynu.cityEase.service.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import nynu.cityEase.api.vo.pms.AuditUserRoomReq;
import nynu.cityEase.api.vo.pms.UserRoomBindReq;
import nynu.cityEase.api.vo.pms.UserRoomQueryReq;
import nynu.cityEase.api.vo.pms.UserRoomVO;
import nynu.cityEase.service.pms.repository.entity.UserRoomRelDO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/3/1
 * Time: 13:20
 * Description: TODO
 */

public interface IPmsUserRoomRelService extends IService<UserRoomRelDO> {

    /**
     * 用户端：提交人房绑定申请
     */
    void submitBindRequest(UserRoomBindReq req);

    /**
     * 物业后台：审核人房绑定申请
     */
    void auditBindRequest(AuditUserRoomReq req);


    /**
     * 分页查询审核列表
     */
    Page<UserRoomVO> getAuditPage(UserRoomQueryReq req);
}