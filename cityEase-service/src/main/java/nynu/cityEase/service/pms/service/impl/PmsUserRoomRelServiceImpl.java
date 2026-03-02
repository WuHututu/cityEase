package nynu.cityEase.service.pms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import nynu.cityEase.api.exception.ExceptionUtil;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PmsUserRoomRelServiceImpl extends ServiceImpl<UserRoomRelMapper, UserRoomRelDO> implements IPmsUserRoomRelService {

    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private UserDao userDao;
    @Autowired
    private IPmsPublicAreaService publicAreaService;

    @Override
    public void submitBindRequest(UserRoomBindReq req) {
        // 1. 获取当前登录用户的 ID (利用你配好的 Sa-Token)
        long currentUserId = StpUtil.getLoginIdAsLong();

        // 2. 校验房屋是否存在
        RoomDO room = roomMapper.selectById(req.getRoomId());
        if (room == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "申请绑定的房屋不存在");
        }

        // 3. 核心业务校验：防重复提交
        UserRoomRelDO existRel = this.getOne(new LambdaQueryWrapper<UserRoomRelDO>()
                .eq(UserRoomRelDO::getUserId, currentUserId)
                .eq(UserRoomRelDO::getRoomId, req.getRoomId())
                .last("limit 1"));

        if (existRel != null) {
            if (existRel.getStatus() == 0) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "您已提交过该房屋的绑定申请，正在等待物业审核，请勿重复提交");
            } else if (existRel.getStatus() == 1) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "您已经是该房屋的认证住户，无需重复绑定");
            } else if (existRel.getStatus() == 2) {
                // 如果之前被驳回了，允许重新提交（可以将旧的记录覆盖，或者把旧记录标为已删除重新插入，这里选择覆盖更新）
                // 重新变为待审核
                existRel.setStatus(0);
                existRel.setRelationType(req.getRelationType());
                existRel.setAttachments(JSONUtil.toJsonStr(req.getAttachments()));
                // 清空上次的驳回原因
                existRel.setRemark("");
                this.updateById(existRel);
                return;
            }
        }

        // 4. 构建并保存新的申请记录
        UserRoomRelDO newRel = new UserRoomRelDO();
        newRel.setUserId(currentUserId);
        newRel.setRoomId(req.getRoomId());
        newRel.setRelationType(req.getRelationType());
        // 0-待审核
        newRel.setStatus(0);
        newRel.setAttachments(JSONUtil.toJsonStr(req.getAttachments()));
        
        this.save(newRel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditBindRequest(AuditUserRoomReq req) {
        // 1. 校验申请记录是否存在
        UserRoomRelDO rel = this.getById(req.getRelId());
        if (rel == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "该申请记录不存在");
        }

        // 2. 状态机防抖校验：只能审核“待审核(0)”状态的数据
        if (rel.getStatus() != 0) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "该申请已被处理，请勿重复操作");
        }

        // 3. 核心流转逻辑
        if (req.getStatus() == 1) {
            // 操作：审核通过
            rel.setStatus(1);
            rel.setRemark("审核通过");

            /*
             * todo 若一个房子只能绑定一个主业主
             *  把该 roomId 下其他处于待审核(0)的申请自动驳回，“该房屋已被他人认证”。
             *  目前允许多人绑定（如夫妻都是业主/家属）。
             */

        } else if (req.getStatus() == 2) {
            // 操作：审核驳回
            if (req.getRemark() == null || req.getRemark().trim().isEmpty()) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "驳回操作必须填写原因");
            }
            rel.setStatus(2);
            rel.setRemark(req.getRemark());
        } else {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "未知的审核状态");
        }

        // 4. 更新到数据库
        this.updateById(rel);

        // todo: 这里可以扩展消息通知功能 (如发送短信、微信公众号推送、站内信) 告诉用户审核结果
    }

    @Override
    public Page<UserRoomVO> getAuditPage(UserRoomQueryReq req) {
        // 1. 构建查询条件
        LambdaQueryWrapper<UserRoomRelDO> wrapper = new LambdaQueryWrapper<>();
        if (req.getStatus() != null) {
            wrapper.eq(UserRoomRelDO::getStatus, req.getStatus());
        }
        if (req.getRelationType() != null) {
            wrapper.eq(UserRoomRelDO::getRelationType, req.getRelationType());
        }
        wrapper.orderByDesc(UserRoomRelDO::getCreateTime); // 最新的申请排在最前面

        // 2. 执行分页查询 (查询数据库 pms_user_room_rel 表)
        Page<UserRoomRelDO> doPage = this.page(new Page<>(req.getPageNo(), req.getPageSize()), wrapper);

        // 3. 将 DO 分页对象转换为 VO 分页对象
        Page<UserRoomVO> voPage = new Page<>(doPage.getCurrent(), doPage.getSize(), doPage.getTotal());
        List<UserRoomVO> voList = new ArrayList<>();

        // 4. 遍历当前页的数据（最多10条），在内存中组装关联信息
        for (UserRoomRelDO relDO : doPage.getRecords()) {
            UserRoomVO vo = new UserRoomVO();
            vo.setRelId(relDO.getId());
            vo.setUserId(relDO.getUserId());
            vo.setRoomId(relDO.getRoomId());
            vo.setRelationType(relDO.getRelationType());
            vo.setStatus(relDO.getStatus());
            vo.setCreateTime(relDO.getCreateTime());

            // 还原附件JSON数组
            if (cn.hutool.core.util.StrUtil.isNotBlank(relDO.getAttachments())) {
                vo.setAttachmentsList(cn.hutool.json.JSONUtil.toList(relDO.getAttachments(), String.class));
            }

            // 4.1 查询申请人信息
            nynu.cityEase.service.user.repository.entity.UserInfoDO userInfo = userDao.getByUserId(relDO.getUserId());
            if (userInfo != null) {
                // 如果填了真实姓名优先展示真实姓名，否则展示昵称
                vo.setUserName(cn.hutool.core.util.StrUtil.isNotBlank(userInfo.getRealName())
                        ? userInfo.getRealName() : userInfo.getUsername());
            }

            // 4.2 查询房屋与地址信息
            RoomDO room = roomMapper.selectById(relDO.getRoomId());
            if (room != null) {
                // 溯源拼接完整的区域名称，例如：江南星城-1号楼-1单元
                String areaFullName = publicAreaService.getFullAreaName(room.getAreaId());
                // 拼接最终的门牌号：江南星城-1号楼-1单元-101
                vo.setFullRoomName(areaFullName + "-" + room.getRoomNum());
            }

            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
    }
}