package nynu.cityEase.service.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import nynu.cityEase.api.vo.pms.AuditUserRoomReq;
import nynu.cityEase.api.vo.pms.UpdateUserRoomAttachmentsReq;
import nynu.cityEase.api.vo.pms.UserRoomAuditDetailVO;
import nynu.cityEase.api.vo.pms.UserRoomBindReq;
import nynu.cityEase.api.vo.pms.UserRoomQueryReq;
import nynu.cityEase.api.vo.pms.UserRoomVO;
import nynu.cityEase.service.pms.repository.entity.UserRoomRelDO;

public interface IPmsUserRoomRelService extends IService<UserRoomRelDO> {

    void submitBindRequest(UserRoomBindReq req);

    void auditBindRequest(AuditUserRoomReq req);

    Page<UserRoomVO> getAuditPage(UserRoomQueryReq req);

    UserRoomAuditDetailVO getAuditDetail(Long relId);

    void updateAuditAttachments(UpdateUserRoomAttachmentsReq req);

    Long getUserRoomId(Long userId);

    boolean bindRoom(Long userId, Long roomId);

    boolean switchCurrentRoom(Long userId, Long roomId);
}
