package nynu.cityEase.service.pms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.MqConstants;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.pms.AuditUserRoomReq;
import nynu.cityEase.api.vo.pms.UserRoomBindReq;
import nynu.cityEase.api.vo.pms.UserRoomQueryReq;
import nynu.cityEase.api.vo.pms.UserRoomVO;
import nynu.cityEase.service.pms.repository.entity.RoomDO;
import nynu.cityEase.service.pms.repository.entity.UserRoomRelDO;
import nynu.cityEase.service.pms.repository.mapper.RoomMapper;
import nynu.cityEase.service.pms.repository.mapper.UserRoomRelMapper;
import nynu.cityEase.service.pms.service.IPmsPublicAreaService;
import nynu.cityEase.service.pms.service.IPmsUserRoomRelService;
import nynu.cityEase.service.user.repository.dao.UserDao;
import nynu.cityEase.service.user.repository.entity.UserInfoDO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PmsUserRoomRelServiceImpl extends ServiceImpl<UserRoomRelMapper, UserRoomRelDO> implements IPmsUserRoomRelService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_REJECTED = 2;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private UserDao userDao;

    @Autowired
    private IPmsPublicAreaService publicAreaService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void submitBindRequest(UserRoomBindReq req) {
        long currentUserId = StpUtil.getLoginIdAsLong();

        RoomDO room = roomMapper.selectById(req.getRoomId());
        if (room == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "申请绑定的房屋不存在");
        }

        UserRoomRelDO existRel = this.getOne(new LambdaQueryWrapper<UserRoomRelDO>()
                .eq(UserRoomRelDO::getUserId, currentUserId)
                .eq(UserRoomRelDO::getRoomId, req.getRoomId())
                .last("limit 1"));

        if (existRel != null) {
            if (existRel.getStatus() == STATUS_PENDING) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "该房屋绑定申请正在审核中，请勿重复提交");
            }
            if (existRel.getStatus() == STATUS_APPROVED) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "您已经是该房屋的认证住户，无需重复绑定");
            }

            existRel.setStatus(STATUS_PENDING);
            existRel.setRelationType(req.getRelationType());
            existRel.setAttachments(JSONUtil.toJsonStr(req.getAttachments()));
            existRel.setRemark("");
            this.updateById(existRel);
            return;
        }

        UserRoomRelDO newRel = new UserRoomRelDO();
        newRel.setUserId(currentUserId);
        newRel.setRoomId(req.getRoomId());
        newRel.setRelationType(req.getRelationType());
        newRel.setStatus(STATUS_PENDING);
        newRel.setAttachments(JSONUtil.toJsonStr(req.getAttachments()));
        this.save(newRel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditBindRequest(AuditUserRoomReq req) {
        UserRoomRelDO rel = this.getById(req.getRelId());
        if (rel == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "申请记录不存在");
        }
        if (rel.getStatus() != STATUS_PENDING) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "该申请已处理，请勿重复操作");
        }

        if (req.getStatus() == STATUS_APPROVED) {
            rel.setStatus(STATUS_APPROVED);
            rel.setRemark("审核通过");
            rel.setIsCurrent(1);
            clearOtherCurrentRooms(rel.getUserId(), rel.getId());
        } else if (req.getStatus() == STATUS_REJECTED) {
            if (StrUtil.isBlank(req.getRemark())) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "驳回时必须填写原因");
            }
            rel.setStatus(STATUS_REJECTED);
            rel.setRemark(req.getRemark().trim());
        } else {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "未知的审核状态");
        }

        this.updateById(rel);
        sendAuditNotify(rel, req);
    }

    @Override
    public Page<UserRoomVO> getAuditPage(UserRoomQueryReq req) {
        LambdaQueryWrapper<UserRoomRelDO> wrapper = new LambdaQueryWrapper<>();
        if (req.getStatus() != null) {
            wrapper.eq(UserRoomRelDO::getStatus, req.getStatus());
        }
        if (req.getRelationType() != null) {
            wrapper.eq(UserRoomRelDO::getRelationType, req.getRelationType());
        }
        wrapper.orderByDesc(UserRoomRelDO::getCreateTime);

        Page<UserRoomRelDO> doPage = this.page(new Page<>(req.getPageNo(), req.getPageSize()), wrapper);
        Page<UserRoomVO> voPage = new Page<>(doPage.getCurrent(), doPage.getSize(), doPage.getTotal());
        List<UserRoomVO> voList = new ArrayList<>(doPage.getRecords().size());

        for (UserRoomRelDO relDO : doPage.getRecords()) {
            UserRoomVO vo = new UserRoomVO();
            vo.setRelId(relDO.getId());
            vo.setUserId(relDO.getUserId());
            vo.setRoomId(relDO.getRoomId());
            vo.setRelationType(relDO.getRelationType());
            vo.setStatus(relDO.getStatus());
            vo.setCreateTime(relDO.getCreateTime());

            if (StrUtil.isNotBlank(relDO.getAttachments())) {
                vo.setAttachmentsList(JSONUtil.toList(relDO.getAttachments(), String.class));
            }

            UserInfoDO userInfo = userDao.getByUserId(relDO.getUserId());
            if (userInfo != null) {
                vo.setUserName(StrUtil.isNotBlank(userInfo.getRealName()) ? userInfo.getRealName() : userInfo.getUsername());
            }

            RoomDO room = roomMapper.selectById(relDO.getRoomId());
            if (room != null) {
                String areaFullName = publicAreaService.getFullAreaName(room.getAreaId());
                vo.setFullRoomName(areaFullName + "-" + room.getRoomNum());
            }

            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Long getUserRoomId(Long userId) {
        UserRoomRelDO rel = this.getOne(new LambdaQueryWrapper<UserRoomRelDO>()
                .eq(UserRoomRelDO::getUserId, userId)
                .eq(UserRoomRelDO::getStatus, STATUS_APPROVED)
                .eq(UserRoomRelDO::getIsCurrent, 1)
                .orderByDesc(UserRoomRelDO::getCreateTime)
                .last("LIMIT 1"));
        return rel != null ? rel.getRoomId() : null;
    }

    @Override
    public boolean bindRoom(Long userId, Long roomId) {
        RoomDO room = roomMapper.selectById(roomId);
        if (room == null) {
            return false;
        }

        UserRoomRelDO existRel = this.getOne(new LambdaQueryWrapper<UserRoomRelDO>()
                .eq(UserRoomRelDO::getUserId, userId)
                .eq(UserRoomRelDO::getRoomId, roomId)
                .eq(UserRoomRelDO::getStatus, STATUS_APPROVED));

        if (existRel != null) {
            existRel.setIsCurrent(1);
            this.updateById(existRel);
            clearOtherCurrentRooms(userId, existRel.getId());
            return true;
        }

        UserRoomRelDO newRel = new UserRoomRelDO();
        newRel.setUserId(userId);
        newRel.setRoomId(roomId);
        newRel.setRelationType(1);
        newRel.setStatus(STATUS_APPROVED);
        newRel.setIsCurrent(1);

        boolean result = this.save(newRel);
        if (result) {
            clearOtherCurrentRooms(userId, newRel.getId());
        }
        return result;
    }

    @Override
    public boolean switchCurrentRoom(Long userId, Long roomId) {
        UserRoomRelDO rel = this.getOne(new LambdaQueryWrapper<UserRoomRelDO>()
                .eq(UserRoomRelDO::getUserId, userId)
                .eq(UserRoomRelDO::getRoomId, roomId)
                .eq(UserRoomRelDO::getStatus, STATUS_APPROVED)
                .last("limit 1"));
        if (rel == null) {
            return false;
        }

        clearOtherCurrentRooms(userId, null);
        rel.setIsCurrent(1);
        return this.updateById(rel);
    }

    private void clearOtherCurrentRooms(Long userId, Long keepRelId) {
        LambdaUpdateWrapper<UserRoomRelDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserRoomRelDO::getUserId, userId)
                .eq(UserRoomRelDO::getStatus, STATUS_APPROVED);
        if (keepRelId != null) {
            updateWrapper.ne(UserRoomRelDO::getId, keepRelId);
        }

        UserRoomRelDO updateDO = new UserRoomRelDO();
        updateDO.setIsCurrent(0);
        this.update(updateDO, updateWrapper);
    }

    private void sendAuditNotify(UserRoomRelDO rel, AuditUserRoomReq req) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("userId", rel.getUserId());
            message.put("type", "room_bind_audit");

            if (req.getStatus() == STATUS_APPROVED) {
                message.put("title", "房屋绑定成功");
                message.put("content", "您的房屋绑定申请已审核通过，现在可以使用相关功能");

                RoomDO room = roomMapper.selectById(rel.getRoomId());
                if (room != null) {
                    String areaFullName = publicAreaService.getFullAreaName(room.getAreaId());
                    message.put("roomInfo", areaFullName + "-" + room.getRoomNum());
                }
            } else {
                message.put("title", "房屋绑定被驳回");
                message.put("content", "您的房屋绑定申请被驳回：" + req.getRemark());
                message.put("rejectReason", req.getRemark());
            }

            rabbitTemplate.convertAndSend(
                    MqConstants.NOTIFY_EXCHANGE,
                    MqConstants.NOTIFY_ROUTING_KEY,
                    message
            );

            log.info("房屋绑定审核通知已发送, userId={}, result={}",
                    rel.getUserId(),
                    req.getStatus() == STATUS_APPROVED ? "approved" : "rejected");
        } catch (Exception e) {
            log.error("发送房屋绑定审核通知失败", e);
        }
    }
}
