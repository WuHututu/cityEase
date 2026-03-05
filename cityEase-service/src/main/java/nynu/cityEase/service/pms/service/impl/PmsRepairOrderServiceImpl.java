package nynu.cityEase.service.pms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.pms.*;
import nynu.cityEase.api.vo.system.NotifyMsgDTO;
import nynu.cityEase.api.vo.user.UserSimpleVO;
import nynu.cityEase.service.pms.repository.entity.RepairOrderDO;
import nynu.cityEase.service.pms.repository.entity.RoomDO;
import nynu.cityEase.service.pms.repository.entity.UserRoomRelDO;
import nynu.cityEase.service.pms.repository.mapper.RepairOrderMapper;
import nynu.cityEase.service.pms.repository.mapper.RoomMapper;
import nynu.cityEase.service.pms.repository.mapper.UserRoomRelMapper;
import nynu.cityEase.service.pms.service.IPmsPublicAreaService;
import nynu.cityEase.service.pms.service.IPmsRepairOrderService;
import nynu.cityEase.service.user.repository.dao.UserDao;
import nynu.cityEase.service.user.repository.entity.UserInfoDO;
import nynu.cityEase.service.user.repository.mapper.UserInfoMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class PmsRepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrderDO> implements IPmsRepairOrderService {
    @Resource
    private RepairOrderMapper repairOrderMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserRoomRelMapper userRoomRelMapper; // pms_user_room_rel

    @Autowired
    private RoomMapper roomMapper;


    // 注入用户Dao获取用户信息
    @Autowired
    private UserDao userDao;

    // 注入公共区域Service获取拼接完整地址
    @Autowired
    private IPmsPublicAreaService publicAreaService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public RepairOrderVO getRepairDetailForAdmin(Long orderId) {
        RepairOrderDO orderDO = this.getById(orderId);
        if (orderDO == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "报修工单不存在");
        }
        return buildVO(orderDO);
    }


    @Override
    public List<UserSimpleVO> listRepairHandlers() {
        // 1) 优先：历史 handler
        List<Long> handlerIds = repairOrderMapper.selectDistinctHandlerIds();
        if (handlerIds != null && !handlerIds.isEmpty()) {
            List<UserInfoDO> users = userInfoMapper.selectBatchIds(handlerIds);
            return users.stream()
                    .filter(u -> u != null && u.getIsDeleted() != null && u.getIsDeleted() == 0)
                    .map(u -> {
                        UserSimpleVO vo = new UserSimpleVO();
                        vo.setUserId(u.getUserId());
                        String name = cn.hutool.core.util.StrUtil.isNotBlank(u.getRealName())
                                ? u.getRealName()
                                : (cn.hutool.core.util.StrUtil.isNotBlank(u.getUsername()) ? u.getUsername() : ("用户" + u.getUserId()));
                        vo.setName(name);
                        return vo;
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        // 2) 兜底：无房屋绑定且非管理员（用 MyBatis-Plus wrapper）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserInfoDO> qw =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        qw.eq(UserInfoDO::getIsDeleted, 0)
                .ne(UserInfoDO::getUserRole, 1)
                // not in 子查询：pms_user_room_rel
                .notInSql(UserInfoDO::getUserId, "SELECT user_id FROM pms_user_room_rel WHERE is_deleted = 0")
                .orderByDesc(UserInfoDO::getUpdateTime)
                .last("LIMIT 50");

        List<UserInfoDO> users = userInfoMapper.selectList(qw);

        return users.stream().map(u -> {
            UserSimpleVO vo = new UserSimpleVO();
            vo.setUserId(u.getUserId());
            String name = cn.hutool.core.util.StrUtil.isNotBlank(u.getRealName())
                    ? u.getRealName()
                    : (cn.hutool.core.util.StrUtil.isNotBlank(u.getUsername()) ? u.getUsername() : ("用户" + u.getUserId()));
            vo.setName(name);
            return vo;
        }).collect(java.util.stream.Collectors.toList());
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRepair(RepairSubmitReq req) {
        
        long userId = StpUtil.getLoginIdAsLong();

        // 校验是否为室内报修，如果是室内报修，必须校验人房关系
        if (req.getRoomId() != null) {
            RoomDO room = roomMapper.selectById(req.getRoomId());
            if (room == null) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "关联的房屋不存在");
            }

            // 查询当前用户是否绑定了该房屋，且审核状态为已通过 (status = 1)
            LambdaQueryWrapper<UserRoomRelDO> relWrapper = new LambdaQueryWrapper<>();
            relWrapper.eq(UserRoomRelDO::getUserId, userId)
                      .eq(UserRoomRelDO::getRoomId, req.getRoomId())
                      .eq(UserRoomRelDO::getStatus, 1)
                      .last("limit 1");
                      
            UserRoomRelDO relDO = userRoomRelMapper.selectOne(relWrapper);
            if (relDO == null) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "您尚未认证该房屋，无法提交室内报修");
            }
        }

        RepairOrderDO orderDO = new RepairOrderDO();
        orderDO.setUserId(userId);
        orderDO.setRoomId(req.getRoomId());
        orderDO.setRepairType(req.getRepairType());
        orderDO.setDescription(req.getDescription());
        
        // 初始状态设定为待派单
        orderDO.setStatus(0);

        // 如果用户上传了图片，将 List 转换为 JSON 字符串存储
        if (CollUtil.isNotEmpty(req.getImages())) {
            orderDO.setImages(JSONUtil.toJsonStr(req.getImages()));
        }

        this.save(orderDO);
    }
    // 请确保引入了 java.time.LocalDateTime

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dispatchOrder(RepairDispatchReq req) {
        RepairOrderDO orderDO = this.getById(req.getOrderId());
        if (orderDO == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "报修工单不存在");
        }

        // 状态机严格防抖校验：必须是“0-待派单”才能被派发
        if (orderDO.getStatus() != 0) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "当前工单状态无法派发，请刷新列表");
        }

        // 更新状态和维修人员ID
        // 状态变更为 1-处理中
        orderDO.setStatus(1);
        orderDO.setHandlerId(req.getHandlerId());
        this.updateById(orderDO);

        // 核心增强：异步发送派单通知给维修师傅
        NotifyMsgDTO notifyMsg = new NotifyMsgDTO();
        notifyMsg.setReceiveUserId(req.getHandlerId());
        notifyMsg.setTitle("新工单派发提醒");
        notifyMsg.setContent("您有一个新的报修工单待处理，单号：" + orderDO.getId());
        notifyMsg.setRelatedBizId(String.valueOf(orderDO.getId()));
        notifyMsg.setNotifyType("REPAIR_DISPATCH");

        rabbitTemplate.convertAndSend(
                nynu.cityEase.api.vo.constants.MqConstants.NOTIFY_EXCHANGE,
                nynu.cityEase.api.vo.constants.MqConstants.NOTIFY_ROUTING_KEY,
                notifyMsg
        );

        log.info("工单 {} 派发成功，已投递异步通知消息", orderDO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(RepairCompleteReq req) {
        RepairOrderDO orderDO = this.getById(req.getOrderId());
        if (orderDO == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "报修工单不存在");
        }

        // 状态机严格防抖校验：必须是“1-处理中”才能提交完成
        if (orderDO.getStatus() != 1) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "工单未在处理中，无法提交处理结果");
        }

        // 更新状态、处理结果、完成时间和处理图片
        // 状态变更为 2-已处理
        orderDO.setStatus(2);
        orderDO.setHandleResult(req.getHandleResult());
        orderDO.setHandleTime(java.time.LocalDateTime.now());

        if (cn.hutool.core.collection.CollUtil.isNotEmpty(req.getHandleImages())) {
            orderDO.setHandleImages(cn.hutool.json.JSONUtil.toJsonStr(req.getHandleImages()));
        }

        this.updateById(orderDO);
    }

    @Override
    public Page<RepairOrderVO> getRepairPage(RepairOrderQueryReq req) {

        // 构建查询条件
        LambdaQueryWrapper<RepairOrderDO> wrapper = new LambdaQueryWrapper<>();

        if (req.getStatus() != null) {
            wrapper.eq(RepairOrderDO::getStatus, req.getStatus());
        }
        if (cn.hutool.core.util.StrUtil.isNotBlank(req.getRepairType())) {
            wrapper.eq(RepairOrderDO::getRepairType, req.getRepairType());
        }

        // 最新的报修单排在最前面
        wrapper.orderByDesc(RepairOrderDO::getCreateTime);

        // 执行数据库分页查询
        Page<RepairOrderDO> doPage = this.page(new Page<>(req.getPageNo(), req.getPageSize()), wrapper);

        // 构建返回的 VO 分页对象 - 确保使用正确的构造函数参数
        Page<RepairOrderVO> voPage = new Page<>();
        voPage.setCurrent(doPage.getCurrent());
        voPage.setSize(doPage.getSize());
        voPage.setTotal(doPage.getTotal());
        
        java.util.List<RepairOrderVO> voList = new java.util.ArrayList<>();

        // 遍历组装数据
        for (RepairOrderDO orderDO : doPage.getRecords()) {
            RepairOrderVO vo = new RepairOrderVO();
            org.springframework.beans.BeanUtils.copyProperties(orderDO, vo);

            // 解析报修现场图片 JSON
            if (cn.hutool.core.util.StrUtil.isNotBlank(orderDO.getImages())) {
                vo.setImagesList(JSONUtil.toList(orderDO.getImages(), String.class));
            }

            // 解析维修结果图片 JSON
            if (cn.hutool.core.util.StrUtil.isNotBlank(orderDO.getHandleImages())) {
                vo.setHandleImagesList(JSONUtil.toList(orderDO.getHandleImages(), String.class));
            }

            // 查询报修人信息
            nynu.cityEase.service.user.repository.entity.UserInfoDO submitter = userDao.getByUserId(orderDO.getUserId());
            if (submitter != null) {
                String name = cn.hutool.core.util.StrUtil.isNotBlank(submitter.getRealName())
                        ? submitter.getRealName() : submitter.getUsername();
                vo.setSubmitterName(name);
            }

            // 查询维修工信息
            if (orderDO.getHandlerId() != null) {
                nynu.cityEase.service.user.repository.entity.UserInfoDO handler = userDao.getByUserId(orderDO.getHandlerId());
                if (handler != null) {
                    String handlerName = cn.hutool.core.util.StrUtil.isNotBlank(handler.getRealName())
                            ? handler.getRealName() : handler.getUsername();
                    vo.setHandlerName(handlerName);
                }
            }

            // 拼接详细地址
            if (orderDO.getRoomId() != null) {
                RoomDO room = roomMapper.selectById(orderDO.getRoomId());
                if (room != null) {
                    String areaFullName = publicAreaService.getFullAreaName(room.getAreaId());
                    vo.setFullAddress(areaFullName + "-" + room.getRoomNum());
                }
            } else {
                vo.setFullAddress("公共区域");
            }

            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
    }

    // 注入我们之前写的积分服务核心类
    @Autowired
    private nynu.cityEase.service.gov.service.IGovPointService govPointService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void evaluateOrder(RepairEvaluateReq req) {

        long userId = StpUtil.getLoginIdAsLong();

        RepairOrderDO orderDO = this.getById(req.getOrderId());
        if (orderDO == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "报修工单不存在");
        }

        // 越权校验：只能评价自己提交的工单
        if (!orderDO.getUserId().equals(userId)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "无权操作他人的工单");
        }

        // 状态机严格校验：必须是“2-已处理”才能进行评价
        if (orderDO.getStatus() != 2) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "工单当前状态无法评价");
        }

        // 星级合法性校验
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "评价星级必须在1到5之间");
        }

        // 1. 更新工单状态为 3-已结单，并写入评价数据
        orderDO.setStatus(3);
        orderDO.setRating(req.getRating());
        orderDO.setEvaluateContent(req.getEvaluateContent());

        this.updateById(orderDO);

        // 2. 跨模块联动：如果该报修是室内报修(有roomId)，给该房屋奖励 5 个积分！
        if (orderDO.getRoomId() != null) {
            govPointService.changePoints(
                    orderDO.getRoomId(),
                    userId,
                    5,
                    true,
                    "参与物业服务评价奖励"
            );
        }
    }

    // 1) 抽取：DO -> VO 组装（复用你现有 getRepairPage 中的逻辑）
    public RepairOrderVO buildVO(RepairOrderDO orderDO) {
        RepairOrderVO vo = new RepairOrderVO();
        org.springframework.beans.BeanUtils.copyProperties(orderDO, vo);

        // 报修现场图片
        if (cn.hutool.core.util.StrUtil.isNotBlank(orderDO.getImages())) {
            vo.setImagesList(cn.hutool.json.JSONUtil.toList(orderDO.getImages(), String.class));
        }
        // 维修结果图片
        if (cn.hutool.core.util.StrUtil.isNotBlank(orderDO.getHandleImages())) {
            vo.setHandleImagesList(cn.hutool.json.JSONUtil.toList(orderDO.getHandleImages(), String.class));
        }

        // 报修人
        nynu.cityEase.service.user.repository.entity.UserInfoDO submitter = userDao.getByUserId(orderDO.getUserId());
        if (submitter != null) {
            String name = cn.hutool.core.util.StrUtil.isNotBlank(submitter.getRealName())
                    ? submitter.getRealName() : submitter.getUsername();
            vo.setSubmitterName(name);
        }

        // 维修工
        if (orderDO.getHandlerId() != null) {
            nynu.cityEase.service.user.repository.entity.UserInfoDO handler = userDao.getByUserId(orderDO.getHandlerId());
            if (handler != null) {
                String handlerName = cn.hutool.core.util.StrUtil.isNotBlank(handler.getRealName())
                        ? handler.getRealName() : handler.getUsername();
                vo.setHandlerName(handlerName);
            }
        }

        // 地址拼接
        if (orderDO.getRoomId() != null) {
            RoomDO room = roomMapper.selectById(orderDO.getRoomId());
            if (room != null) {
                String areaFullName = publicAreaService.getFullAreaName(room.getAreaId());
                vo.setFullAddress(areaFullName + "-" + room.getRoomNum());
            }
        } else {
            vo.setFullAddress("公共区域");
        }

        return vo;
    }

    // 2) App端：我的报修分页
    @Override
    public Page<RepairOrderVO> getMyRepairPage(RepairMyOrderQueryReq req) {
        long userId = StpUtil.getLoginIdAsLong();

        LambdaQueryWrapper<RepairOrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairOrderDO::getUserId, userId);

        if (req.getStatus() != null) {
            wrapper.eq(RepairOrderDO::getStatus, req.getStatus());
        }
        if (cn.hutool.core.util.StrUtil.isNotBlank(req.getRepairType())) {
            wrapper.eq(RepairOrderDO::getRepairType, req.getRepairType());
        }
        wrapper.orderByDesc(RepairOrderDO::getCreateTime);

        Page<RepairOrderDO> page = new Page<>(req.getPageNo(), req.getPageSize());
        Page<RepairOrderDO> doPage = this.page(page, wrapper);

        Page<RepairOrderVO> voPage = new Page<>(doPage.getCurrent(), doPage.getSize(), doPage.getTotal());
        java.util.List<RepairOrderVO> vos = new java.util.ArrayList<>();
        for (RepairOrderDO o : doPage.getRecords()) {
            vos.add(buildVO(o));
        }
        voPage.setRecords(vos);
        return voPage;
    }

    // 3) App端：我的工单详情（必须本人）
    @Override
    public RepairOrderVO getMyRepairDetail(Long orderId) {
        long userId = StpUtil.getLoginIdAsLong();
        RepairOrderDO orderDO = this.getById(orderId);
        if (orderDO == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "报修工单不存在");
        }
        if (!orderDO.getUserId().equals(userId)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "无权查看他人的工单");
        }
        return buildVO(orderDO);
    }

    // 4) App端：取消工单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(RepairCancelReq req) {
        long userId = StpUtil.getLoginIdAsLong();
        RepairOrderDO orderDO = this.getById(req.getOrderId());
        if (orderDO == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "报修工单不存在");
        }
        if (!orderDO.getUserId().equals(userId)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "无权操作他人的工单");
        }

        // 状态机：仅允许 未派单(0) / 处理中(1) 取消（你也可以只允许 0）
        if (orderDO.getStatus() == null || (orderDO.getStatus() != 0 && orderDO.getStatus() != 1)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "当前工单状态无法取消");
        }

        orderDO.setStatus(4); // 4-已取消（你的 DO 注释里定义了 4）:contentReference[oaicite:8]{index=8}
        // 可选：如果你想记录取消原因，建议以后加字段 cancelReason，这里先不写库字段
        this.updateById(orderDO);
    }

    // 5) 维修人员：我被派发的工单分页
    @Override
    public Page<RepairOrderVO> getMyAssignedPage(RepairHandlerOrderQueryReq req) {
        long handlerId = StpUtil.getLoginIdAsLong();

        LambdaQueryWrapper<RepairOrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairOrderDO::getHandlerId, handlerId);

        if (req.getStatus() != null) {
            wrapper.eq(RepairOrderDO::getStatus, req.getStatus());
        } else {
            // 默认给维修人员看：处理中/已处理
            wrapper.in(RepairOrderDO::getStatus, 1, 2);
        }
        wrapper.orderByDesc(RepairOrderDO::getCreateTime);

        Page<RepairOrderDO> page = new Page<>(req.getPageNo(), req.getPageSize());
        Page<RepairOrderDO> doPage = this.page(page, wrapper);

        Page<RepairOrderVO> voPage = new Page<>(doPage.getCurrent(), doPage.getSize(), doPage.getTotal());
        java.util.List<RepairOrderVO> vos = new java.util.ArrayList<>();
        for (RepairOrderDO o : doPage.getRecords()) {
            vos.add(buildVO(o));
        }
        voPage.setRecords(vos);
        return voPage;
    }


}