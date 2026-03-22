package nynu.cityEase.service.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import nynu.cityEase.api.vo.pms.AuditUserRoomReq;
import nynu.cityEase.api.vo.pms.UserRoomBindReq;
import nynu.cityEase.api.vo.pms.UserRoomQueryReq;
import nynu.cityEase.api.vo.pms.UserRoomVO;
//import nynu.cityEase.api.vo.user.UserRoomRelVO;
import nynu.cityEase.service.pms.repository.entity.UserRoomRelDO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;


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

    /**
     * 获取用户的房屋ID（主要房屋）
     */
    Long getUserRoomId(Long userId);

    /**
     * 获取用户的房屋列表
     */
//    List<UserRoomRelVO> getUserRooms(Long userId);
//
//    /**
//     * 获取用户当前绑定的房屋
//     */
//    UserRoomRelVO getUserCurrentRoom(Long userId);

    /**
     * 绑定房屋
     */
    boolean bindRoom(Long userId, Long roomId);

    /**
     * 切换当前房屋
     */
    boolean switchCurrentRoom(Long userId, Long roomId);
}