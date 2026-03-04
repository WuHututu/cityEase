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
import nynu.cityEase.service.pms.repository.entity.RepairOrderDO;
import nynu.cityEase.service.pms.repository.entity.RoomDO;
import nynu.cityEase.service.pms.repository.entity.UserRoomRelDO;
import nynu.cityEase.service.pms.repository.mapper.RepairOrderMapper;
import nynu.cityEase.service.pms.repository.mapper.RoomMapper;
import nynu.cityEase.service.pms.repository.mapper.UserRoomRelMapper;
import nynu.cityEase.service.pms.service.IPmsPublicAreaService;
import nynu.cityEase.service.pms.service.IPmsRepairOrderService;
import nynu.cityEase.service.user.repository.dao.UserDao;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PmsRepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrderDO> implements IPmsRepairOrderService {

    @Autowired
    private RoomMapper roomMapper;
    
    @Autowired
    private UserRoomRelMapper userRoomRelMapper;

    // 注入用户Dao获取用户信息
    @Autowired
    private UserDao userDao;

    // 注入公共区域Service获取拼接完整地址
    @Autowired
    private IPmsPublicAreaService publicAreaService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

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

        // 构建返回的 VO 分页对象
        Page<RepairOrderVO> voPage = new Page<>(doPage.getCurrent(), doPage.getSize(), doPage.getTotal());
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

}