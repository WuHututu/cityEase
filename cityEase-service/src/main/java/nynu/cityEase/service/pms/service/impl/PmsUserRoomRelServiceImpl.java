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
import nynu.cityEase.api.vo.pms.UpdateUserRoomAttachmentsReq;
import nynu.cityEase.api.vo.pms.UserRoomAuditDetailVO;
import nynu.cityEase.api.vo.pms.UserRoomBindReq;
import nynu.cityEase.api.vo.pms.UserRoomQueryReq;
import nynu.cityEase.api.vo.pms.UserRoomVO;
import nynu.cityEase.service.pms.repository.entity.RoomDO;
import nynu.cityEase.service.pms.repository.entity.UserRoomRelDO;
import nynu.cityEase.service.pms.repository.mapper.RoomMapper;
import nynu.cityEase.service.pms.repository.mapper.UserRoomRelMapper;
import nynu.cityEase.service.pms.service.IPmsPublicAreaService;
import nynu.cityEase.service.pms.service.IPmsUserRoomRelService;
import nynu.cityEase.service.pms.service.entity.BindAuditDO;
import nynu.cityEase.service.pms.service.mapper.BindAuditMapper;
import nynu.cityEase.service.user.repository.dao.UserDao;
import nynu.cityEase.service.user.repository.entity.UserDO;
import nynu.cityEase.service.user.repository.entity.UserInfoDO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Autowired
    private BindAuditMapper bindAuditMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitBindRequest(UserRoomBindReq req) {
        long currentUserId = StpUtil.getLoginIdAsLong();
        RoomDO room = requireRoom(req.getRoomId());

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
            existRel.setAttachments(serializeAttachments(req.getAttachments()));
            existRel.setRemark("");
            existRel.setIsCurrent(0);
            this.updateById(existRel);
            createAuditSnapshot(existRel, room);
            return;
        }

        UserRoomRelDO newRel = new UserRoomRelDO();
        newRel.setUserId(currentUserId);
        newRel.setRoomId(req.getRoomId());
        newRel.setRelationType(req.getRelationType());
        newRel.setStatus(STATUS_PENDING);
        newRel.setAttachments(serializeAttachments(req.getAttachments()));
        newRel.setIsCurrent(0);
        this.save(newRel);

        createAuditSnapshot(newRel, room);
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
            rel.setIsCurrent(0);
        } else {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "未知的审核状态");
        }

        this.updateById(rel);
        completeAuditSnapshot(rel);
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
            voList.add(buildAuditPageVO(relDO));
        }

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public UserRoomAuditDetailVO getAuditDetail(Long relId) {
        UserRoomRelDO rel = this.getById(relId);
        if (rel == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "绑定申请不存在");
        }

        BindAuditDO snapshot = getLatestAuditSnapshot(relId);
        return buildAuditDetailVO(rel, snapshot);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAuditAttachments(UpdateUserRoomAttachmentsReq req) {
        if (req.getRelId() == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "缺少绑定申请ID");
        }

        UserRoomRelDO rel = this.getById(req.getRelId());
        if (rel == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "绑定申请不存在");
        }

        rel.setAttachments(serializeAttachments(req.getAttachments()));
        this.updateById(rel);
        syncAuditSnapshotAttachments(rel);
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

    private UserRoomVO buildAuditPageVO(UserRoomRelDO relDO) {
        UserRoomVO vo = new UserRoomVO();
        vo.setRelId(relDO.getId());
        vo.setUserId(relDO.getUserId());
        vo.setRoomId(relDO.getRoomId());
        vo.setRelationType(relDO.getRelationType());
        vo.setStatus(relDO.getStatus());
        vo.setCreateTime(relDO.getCreateTime());
        vo.setAttachmentsList(parseAttachments(relDO.getAttachments()));

        UserInfoDO userInfo = userDao.getByUserId(relDO.getUserId());
        UserDO user = userDao.getUserById(relDO.getUserId());
        vo.setUserName(resolveDisplayName(userInfo, user, relDO.getUserId()));

        RoomDO room = roomMapper.selectById(relDO.getRoomId());
        vo.setFullRoomName(buildRoomFullName(room));
        return vo;
    }

    private UserRoomAuditDetailVO buildAuditDetailVO(UserRoomRelDO rel, BindAuditDO snapshot) {
        UserRoomAuditDetailVO vo = new UserRoomAuditDetailVO();
        vo.setRelId(rel.getId());
        vo.setUserId(rel.getUserId());
        vo.setRoomId(rel.getRoomId());
        vo.setRelationType(rel.getRelationType());
        vo.setStatus(rel.getStatus());
        vo.setCreateTime(rel.getCreateTime());

        UserInfoDO userInfo = userDao.getByUserId(rel.getUserId());
        UserDO user = userDao.getUserById(rel.getUserId());
        vo.setUserName(resolveDisplayName(userInfo, user, rel.getUserId()));
        vo.setPhone(user != null ? user.getPhone() : "");

        RoomDO room = roomMapper.selectById(rel.getRoomId());
        vo.setFullRoomName(buildRoomFullName(room));

        if (snapshot != null) {
            vo.setAuditId(snapshot.getId());
            vo.setAuditorName(snapshot.getAuditorName());
            vo.setAuditTime(snapshot.getAuditTime());
            vo.setRemark(snapshot.getRemark());
            vo.setAttachmentsList(parseAttachments(snapshot.getAttachments()));
        } else {
            vo.setAttachmentsList(parseAttachments(rel.getAttachments()));
            vo.setRemark(rel.getRemark());
        }

        return vo;
    }

    private void createAuditSnapshot(UserRoomRelDO rel, RoomDO room) {
        BindAuditDO snapshot = buildAuditSnapshot(rel, room);
        snapshot.setStatus(STATUS_PENDING);
        snapshot.setApplyTime(LocalDateTime.now());
        snapshot.setAuditorId(null);
        snapshot.setAuditorName(null);
        snapshot.setAuditTime(null);
        bindAuditMapper.insert(snapshot);
    }

    private void completeAuditSnapshot(UserRoomRelDO rel) {
        BindAuditDO snapshot = getLatestAuditSnapshot(rel.getId());
        if (snapshot == null) {
            snapshot = buildAuditSnapshot(rel, roomMapper.selectById(rel.getRoomId()));
            snapshot.setApplyTime(rel.getCreateTime() != null ? rel.getCreateTime() : LocalDateTime.now());
            fillAuditorInfo(snapshot);
            bindAuditMapper.insert(snapshot);
            return;
        }

        snapshot.setStatus(rel.getStatus());
        snapshot.setAttachments(normalizeAttachments(rel.getAttachments()));
        snapshot.setRemark(rel.getRemark());
        fillAuditorInfo(snapshot);
        bindAuditMapper.updateById(snapshot);
    }

    private void syncAuditSnapshotAttachments(UserRoomRelDO rel) {
        BindAuditDO snapshot = getLatestAuditSnapshot(rel.getId());
        if (snapshot == null) {
            snapshot = buildAuditSnapshot(rel, roomMapper.selectById(rel.getRoomId()));
            snapshot.setApplyTime(rel.getCreateTime() != null ? rel.getCreateTime() : LocalDateTime.now());
            bindAuditMapper.insert(snapshot);
            return;
        }

        snapshot.setAttachments(normalizeAttachments(rel.getAttachments()));
        snapshot.setRemark(rel.getRemark());
        bindAuditMapper.updateById(snapshot);
    }

    private BindAuditDO buildAuditSnapshot(UserRoomRelDO rel, RoomDO room) {
        UserInfoDO userInfo = userDao.getByUserId(rel.getUserId());
        UserDO user = userDao.getUserById(rel.getUserId());

        BindAuditDO snapshot = new BindAuditDO();
        snapshot.setRelId(rel.getId());
        snapshot.setOwnerId(rel.getUserId());
        snapshot.setRoomId(rel.getRoomId());
        snapshot.setOwnerName(resolveDisplayName(userInfo, user, rel.getUserId()));
        snapshot.setPhone(user != null ? user.getPhone() : "");
        snapshot.setRoomInfo(buildRoomFullName(room));
        snapshot.setAttachments(normalizeAttachments(rel.getAttachments()));
        snapshot.setStatus(rel.getStatus());
        snapshot.setRemark(rel.getRemark());
        return snapshot;
    }

    private BindAuditDO getLatestAuditSnapshot(Long relId) {
        return bindAuditMapper.selectOne(new LambdaQueryWrapper<BindAuditDO>()
                .eq(BindAuditDO::getRelId, relId)
                .orderByDesc(BindAuditDO::getApplyTime)
                .orderByDesc(BindAuditDO::getCreateTime)
                .last("limit 1"));
    }

    private void fillAuditorInfo(BindAuditDO snapshot) {
        long auditorId = StpUtil.getLoginIdAsLong();
        snapshot.setAuditorId(auditorId);

        UserInfoDO auditorInfo = userDao.getByUserId(auditorId);
        UserDO auditorUser = userDao.getUserById(auditorId);
        snapshot.setAuditorName(resolveDisplayName(auditorInfo, auditorUser, auditorId));
        snapshot.setAuditTime(LocalDateTime.now());
    }

    private RoomDO requireRoom(Long roomId) {
        RoomDO room = roomMapper.selectById(roomId);
        if (room == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "申请绑定的房屋不存在");
        }
        return room;
    }

    private String buildRoomFullName(RoomDO room) {
        if (room == null) {
            return "-";
        }
        String areaFullName = publicAreaService.getFullAreaName(room.getAreaId());
        if (StrUtil.isBlank(areaFullName)) {
            return room.getRoomNum();
        }
        return areaFullName + "-" + room.getRoomNum();
    }

    private String resolveDisplayName(UserInfoDO userInfo, UserDO user, Long fallbackId) {
        if (userInfo != null) {
            if (StrUtil.isNotBlank(userInfo.getRealName())) {
                return userInfo.getRealName();
            }
            if (StrUtil.isNotBlank(userInfo.getUsername())) {
                return userInfo.getUsername();
            }
        }
        if (user != null && StrUtil.isNotBlank(user.getPhone())) {
            return user.getPhone();
        }
        return String.valueOf(fallbackId);
    }

    private String serializeAttachments(List<String> attachments) {
        List<String> safeList = new ArrayList<>();
        if (attachments != null) {
            for (String attachment : attachments) {
                if (StrUtil.isNotBlank(attachment) && !safeList.contains(attachment)) {
                    safeList.add(attachment.trim());
                }
            }
        }
        return JSONUtil.toJsonStr(safeList);
    }

    private String normalizeAttachments(String attachments) {
        return serializeAttachments(parseAttachments(attachments));
    }

    private List<String> parseAttachments(String attachments) {
        if (StrUtil.isBlank(attachments)) {
            return new ArrayList<>();
        }
        try {
            return JSONUtil.toList(attachments, String.class);
        } catch (Exception e) {
            log.warn("解析绑定证明材料失败: {}", attachments, e);
            return new ArrayList<>();
        }
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
                    message.put("roomInfo", buildRoomFullName(room));
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

            log.info("房屋绑定审核通知已发送 userId={}, result={}",
                    rel.getUserId(),
                    req.getStatus() == STATUS_APPROVED ? "approved" : "rejected");
        } catch (Exception e) {
            log.error("发送房屋绑定审核通知失败", e);
        }
    }
}
