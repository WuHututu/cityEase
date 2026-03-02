package nynu.cityEase.service.gov.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.gov.PointRedeemReq;
import nynu.cityEase.service.gov.repository.entity.GovPointGoodsDO;
import nynu.cityEase.service.gov.repository.entity.GovPointOrderDO;
import nynu.cityEase.service.gov.repository.mapper.GovPointGoodsMapper;
import nynu.cityEase.service.gov.repository.mapper.GovPointOrderMapper;
import nynu.cityEase.service.gov.service.IGovPointGoodsService;
import nynu.cityEase.service.gov.service.IGovPointService;
import nynu.cityEase.service.pms.repository.entity.UserRoomRelDO;
import nynu.cityEase.service.pms.service.IPmsUserRoomRelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GovPointGoodsServiceImpl extends ServiceImpl<GovPointGoodsMapper, GovPointGoodsDO> implements IGovPointGoodsService {

    @Autowired
    private IGovPointService govPointService;
    @Autowired
    private GovPointOrderMapper orderMapper;
    @Autowired
    private IPmsUserRoomRelService userRoomRelService;

    @Override
    @Transactional(rollbackFor = Exception.class) // ★ 极其重要的事务注解
    public void redeemGoods(PointRedeemReq req) {
        long userId = StpUtil.getLoginIdAsLong();

        // 1. 权限校验：利用userRoomRelService 来查该用户是否有权使用这个房屋的积分(必须是审核通过的业主/家属)
        // 伪代码: checkUserRoomPermission(userId, req.getRoomId());

        // 2. 查询商品基本信息
        GovPointGoodsDO goods = this.getById(req.getGoodsId());
        if (goods == null || goods.getStatus() == 0) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "商品不存在或已下架");
        }
        if (goods.getStock() <= 0) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "商品库存不足，手慢无~");
        }

        // 3. 扣减积分 (复用安全底座，如果余额不足，它会自动抛出异常并回滚整个事务！)
        govPointService.changePoints(
                req.getRoomId(), 
                userId, 
                goods.getPointsPrice(), 
                false, 
                "积分商城兑换：" + goods.getName()
        );

        // 4. 原子级扣减库存 (防超卖核心代码)
        // UPDATE gov_point_goods SET stock = stock - 1 WHERE id = ? AND stock > 0
        int updateRows = this.baseMapper.update(null, new LambdaUpdateWrapper<GovPointGoodsDO>()
                .eq(GovPointGoodsDO::getId, goods.getId())
                // 必须保证库存大于0才能扣
                .gt(GovPointGoodsDO::getStock, 0)
                .setSql("stock = stock - 1")
        );
        
        if (updateRows == 0) {
            // 走到这里说明刚才查还有库存，但正准备扣的时候被别人抢光了。
            // 抛出异常，触发事务回滚，刚才第3步扣掉的积分会自动退还！
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "哎呀，商品刚刚被抢光了！");
        }

        // 5. 生成兑换订单
        GovPointOrderDO order = new GovPointOrderDO();
        order.setGoodsId(goods.getId());
        order.setGoodsName(goods.getName());
        order.setRoomId(req.getRoomId());
        order.setUserId(userId);
        order.setPointsPaid(goods.getPointsPrice());
        // 待核销(用户需拿着手机去物业办公室领实体物品)
        order.setStatus(0);
        
        orderMapper.insert(order);
    }
}