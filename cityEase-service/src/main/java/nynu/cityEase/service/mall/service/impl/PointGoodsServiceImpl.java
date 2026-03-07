package nynu.cityEase.service.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import nynu.cityEase.api.vo.StatusEnum;
import nynu.cityEase.api.vo.mall.PointGoodsPageReq;
import nynu.cityEase.api.vo.mall.PointGoodsSaveReq;
import nynu.cityEase.api.vo.mall.PointGoodsVO;
import nynu.cityEase.service.mall.entity.PointGoodsDO;
import nynu.cityEase.service.mall.mapper.PointGoodsMapper;
import nynu.cityEase.service.mall.service.IPointGoodsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class PointGoodsServiceImpl extends ServiceImpl<PointGoodsMapper, PointGoodsDO> implements IPointGoodsService {

    private static PointGoodsVO toVO(PointGoodsDO d) {
        if (d == null) return null;
        PointGoodsVO vo = new PointGoodsVO();
        vo.setId(d.getId());
        vo.setName(d.getName());
        vo.setDescription(d.getDescription());
        vo.setImageUrl(d.getImageUrl());
        vo.setPoints(d.getPoints());
        vo.setStock(d.getStock());
        vo.setStatus(d.getStatus());
        vo.setCreateTime(d.getCreateTime());
        vo.setUpdateTime(d.getUpdateTime());
        return vo;
    }

    @Override
    public IPage<PointGoodsVO> page(PointGoodsPageReq req) {
        Page<PointGoodsDO> page = new Page<>(req.getCurrent(), req.getSize());

        LambdaQueryWrapper<PointGoodsDO> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(req.getKeyword())) {
            qw.like(PointGoodsDO::getName, req.getKeyword());
        }
        if (req.getStatus() != null) {
            qw.eq(PointGoodsDO::getStatus, req.getStatus());
        }
        qw.orderByDesc(PointGoodsDO::getUpdateTime).orderByDesc(PointGoodsDO::getCreateTime);

        IPage<PointGoodsDO> p = this.baseMapper.selectPage(page, qw);

        // map to VO
        Page<PointGoodsVO> out = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        out.setRecords(p.getRecords().stream().map(PointGoodsServiceImpl::toVO).toList());
        return out;
    }

    @Override
    public PointGoodsVO detail(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(StatusEnum.valueOf("缺少商品ID").getMsg());
        }
        return toVO(this.baseMapper.selectById(id));
    }

    @Override
    public void saveOrUpdate(PointGoodsSaveReq req) {
        if (req == null) {
            throw new IllegalArgumentException(StatusEnum.valueOf("参数错误").getMsg());
        }
        if (!StringUtils.hasText(req.getName())) {
            throw new IllegalArgumentException(StatusEnum.valueOf("缺少商品名称").getMsg());
        }
        if (req.getPoints() == null) {
            throw new IllegalArgumentException(StatusEnum.valueOf("缺少积分").getMsg());
        }
        if (req.getStock() == null) req.setStock(0);
        if (req.getStatus() == null) req.setStatus(0);

        LocalDateTime now = LocalDateTime.now();
        if (req.getId() == null) {
            PointGoodsDO d = new PointGoodsDO();
            d.setName(req.getName());
            d.setDescription(req.getDescription());
            d.setImageUrl(req.getImageUrl());
            d.setPoints(req.getPoints());
            d.setStock(req.getStock());
            d.setStatus(req.getStatus());
            d.setCreateTime(now);
            d.setUpdateTime(now);
            this.baseMapper.insert(d);
            return;
        }

        PointGoodsDO d = this.baseMapper.selectById(req.getId());
        if (d == null) {
            throw new IllegalArgumentException(StatusEnum.valueOf("商品不存在").getMsg());
        }
        d.setName(req.getName());
        d.setDescription(req.getDescription());
        d.setImageUrl(req.getImageUrl());
        d.setPoints(req.getPoints());
        d.setStock(req.getStock());
        d.setStatus(req.getStatus());
        d.setUpdateTime(now);
        this.baseMapper.updateById(d);
    }

    @Override
    public void removeById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(StatusEnum.valueOf("缺少商品ID").getMsg());
        }
        this.baseMapper.deleteById(id);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        if (id == null) {
            throw new IllegalArgumentException(StatusEnum.valueOf("缺少商品ID").getMsg());
        }
        if (status == null) {
            throw new IllegalArgumentException(StatusEnum.valueOf("缺少状态").getMsg());
        }
        PointGoodsDO d = this.baseMapper.selectById(id);
        if (d == null) {
            throw new IllegalArgumentException(StatusEnum.valueOf("商品不存在").getMsg());
        }
        d.setStatus(status);
        d.setUpdateTime(LocalDateTime.now());
        this.baseMapper.updateById(d);
    }
}
