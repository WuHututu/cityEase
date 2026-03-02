package nynu.cityEase.service.system.notice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.RedisKeyConstants;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.system.AppNoticeQueryReq;
import nynu.cityEase.api.vo.system.NoticeDetailVO;
import nynu.cityEase.api.vo.system.NoticeListVO;
import nynu.cityEase.api.vo.system.NoticeSaveReq;
import nynu.cityEase.service.system.notice.repository.entity.SysNoticeDO;
import nynu.cityEase.service.system.notice.repository.mapper.SysNoticeMapper;
import nynu.cityEase.service.system.notice.service.ISysNoticeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNoticeDO> implements ISysNoticeService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateNotice(NoticeSaveReq req) {
        
        long adminId = StpUtil.getLoginIdAsLong();
        
        SysNoticeDO noticeDO = new SysNoticeDO();
        BeanUtils.copyProperties(req, noticeDO);
        
        // 提取前端的发布意图：1代表已发布，0代表草稿
        int targetStatus = req.getIsPublish() ? 1 : 0;
        noticeDO.setStatus(targetStatus);

        if (req.getId() == null) {
            // 新增逻辑
            noticeDO.setCreatorId(adminId);
            this.save(noticeDO);
        } else {
            // 修改逻辑
            SysNoticeDO existNotice = this.getById(req.getId());
            if (existNotice == null) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "该公告不存在或已被删除");
            }
            
            noticeDO.setCreatorId(existNotice.getCreatorId());
            this.updateById(noticeDO);
        }
    }

    @Override
    public Page<NoticeListVO> getPublishedNotices(AppNoticeQueryReq req) {

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysNoticeDO> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        // 核心安全控制：App端永远只能查到 status = 1 (已发布) 的公告
        wrapper.eq(SysNoticeDO::getStatus, 1);

        if (req.getNoticeType() != null) {
            wrapper.eq(SysNoticeDO::getNoticeType, req.getNoticeType());
        }

        // 按发布时间倒序排列
        wrapper.orderByDesc(SysNoticeDO::getCreateTime);

        // 性能优化：明确指定只查小字段，绝对不查 notice_content 这个大文本字段
        wrapper.select(SysNoticeDO::getId, SysNoticeDO::getNoticeTitle, SysNoticeDO::getNoticeType, SysNoticeDO::getCreateTime);

        Page<SysNoticeDO> doPage = this.page(new Page<>(req.getPageNo(), req.getPageSize()), wrapper);

        Page<NoticeListVO> voPage = new Page<>(doPage.getCurrent(), doPage.getSize(), doPage.getTotal());
        java.util.List<NoticeListVO> voList = new java.util.ArrayList<>();

        for (SysNoticeDO noticeDO : doPage.getRecords()) {
            NoticeListVO vo = new NoticeListVO();
            org.springframework.beans.BeanUtils.copyProperties(noticeDO, vo);
            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public NoticeDetailVO getNoticeDetail(Long id) {

        // 定义 Redis Key
        String redisKey = RedisKeyConstants.NOTICE_DETAIL_KEY + id;

        // 1. 优先查询 Redis 缓存
        String cacheJson = stringRedisTemplate.opsForValue().get(redisKey);

        if (cn.hutool.core.util.StrUtil.isNotBlank(cacheJson)) {
            // 缓存命中，直接反序列化返回
            return cn.hutool.json.JSONUtil.toBean(cacheJson, NoticeDetailVO.class);
        }

        // 2. 缓存未命中，穿透到数据库查询
        SysNoticeDO noticeDO = this.getById(id);

        if (noticeDO == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "公告不存在");
        }

        //防止抓包
        if (noticeDO.getStatus() != 1) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "该公告尚未发布或已撤回");
        }

        NoticeDetailVO vo = new NoticeDetailVO();
        org.springframework.beans.BeanUtils.copyProperties(noticeDO, vo);

        // 3. 将查询结果写入 Redis，并设置 24 小时过期时间防止内存堆积
        stringRedisTemplate.opsForValue().set(
                redisKey,
                cn.hutool.json.JSONUtil.toJsonStr(vo),
                24,
                java.util.concurrent.TimeUnit.HOURS
        );

        return vo;
    }
}