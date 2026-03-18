package nynu.cityEase.service.system.notice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.RedisKeyConstants;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.system.AdminNoticeQueryReq;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 公告服务实现
 */
@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNoticeDO> implements ISysNoticeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateNotice(NoticeSaveReq req) {
        long adminId = StpUtil.getLoginIdAsLong();
    
        SysNoticeDO noticeDO = new SysNoticeDO();
        BeanUtils.copyProperties(req, noticeDO);
    
        // 提取前端的发布意图：1 代表已发布，0 代表草稿
        int targetStatus = Boolean.TRUE.equals(req.getIsPublish()) ? 1 : 0;
        noticeDO.setStatus(targetStatus);
    
        if (req.getId() == null) {
            // 新增
            noticeDO.setCreatorId(adminId);
            this.save(noticeDO);
        } else {
            // 修改
            SysNoticeDO existNotice = this.getById(req.getId());
            if (existNotice == null) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "该公告不存在或已被删除");
            }
            // 保留原创建人
            noticeDO.setCreatorId(existNotice.getCreatorId());
            this.updateById(noticeDO);
                
            // 修改后清理缓存，防止缓存导致的状态不一致
            stringRedisTemplate.delete(RedisKeyConstants.NOTICE_DETAIL_KEY + req.getId());
        }
    }

    @Override
    public Page<NoticeListVO> getPublishedNotices(AppNoticeQueryReq req) {
        LambdaQueryWrapper<SysNoticeDO> wrapper = new LambdaQueryWrapper<>();

        // 核心安全控制：App端永远只能查到 status = 1 (已发布) 的公告
        wrapper.eq(SysNoticeDO::getStatus, 1);

        if (req.getNoticeType() != null) {
            wrapper.eq(SysNoticeDO::getNoticeType, req.getNoticeType());
        }

        // 按发布时间倒序排列
        wrapper.orderByDesc(SysNoticeDO::getCreateTime);

        // 性能优化：明确指定只查小字段，绝对不查 notice_content 这个大文本字段
        wrapper.select(
                SysNoticeDO::getId,
                SysNoticeDO::getNoticeTitle,
                SysNoticeDO::getNoticeType,
                SysNoticeDO::getCreateTime
        );

        Page<SysNoticeDO> doPage = this.page(new Page<>(req.getPageNo(), req.getPageSize()), wrapper);

        Page<NoticeListVO> voPage = new Page<>(doPage.getCurrent(), doPage.getSize(), doPage.getTotal());
        List<NoticeListVO> voList = new ArrayList<>(doPage.getRecords().size());

        for (SysNoticeDO noticeDO : doPage.getRecords()) {
            NoticeListVO vo = new NoticeListVO();
            BeanUtils.copyProperties(noticeDO, vo);
            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public NoticeDetailVO getNoticeDetail(Long id) {
        // 定义 Redis Key
        String redisKey = RedisKeyConstants.NOTICE_DETAIL_KEY + id;
    
        // 1) 优先查询 Redis 缓存
        String cacheJson = stringRedisTemplate.opsForValue().get(redisKey);
        if (StrUtil.isNotBlank(cacheJson)) {
            return JSONUtil.toBean(cacheJson, NoticeDetailVO.class);
        }
    
        // 2) 缓存未命中，穿透到数据库查询
        SysNoticeDO noticeDO = this.getById(id);
        if (noticeDO == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "公告不存在");
        }
            
        // 注意：管理端后台可以查看任何状态的公告（包括草稿），所以这里不做状态限制
        // App 端的访问限制应该在 App 端接口中做，而不是在管理端
            
        NoticeDetailVO vo = new NoticeDetailVO();
        BeanUtils.copyProperties(noticeDO, vo);
    
        // 3) 将查询结果写入 Redis，并设置 24 小时过期时间
        stringRedisTemplate.opsForValue().set(redisKey, JSONUtil.toJsonStr(vo), 24, TimeUnit.HOURS);
    
        return vo;
    }

    @Override
    public Page<NoticeListVO> page(AdminNoticeQueryReq req) {
        LambdaQueryWrapper<SysNoticeDO> wrapper = new LambdaQueryWrapper<>();

        // 按类型筛选
        if (req.getNoticeType() != null) {
            wrapper.eq(SysNoticeDO::getNoticeType, req.getNoticeType());
        }

        // 按状态筛选
        if (req.getStatus() != null) {
            wrapper.eq(SysNoticeDO::getStatus, req.getStatus());
        }

        // 标题关键字模糊搜索
        if (StrUtil.isNotBlank(req.getKeyword())) {
            wrapper.like(SysNoticeDO::getNoticeTitle, req.getKeyword().trim());
        }

        // 排序：先按置顶降序，再按创建时间倒序
        wrapper.orderByDesc(SysNoticeDO::getIsTop)
                .orderByDesc(SysNoticeDO::getCreateTime);

        Page<SysNoticeDO> doPage = this.page(new Page<>(req.getPageNo(), req.getPageSize()), wrapper);

        Page<NoticeListVO> voPage = new Page<>(doPage.getCurrent(), doPage.getSize(), doPage.getTotal());
        List<NoticeListVO> voList = new ArrayList<>(doPage.getRecords().size());

        for (SysNoticeDO noticeDO : doPage.getRecords()) {
            NoticeListVO vo = new NoticeListVO();
            BeanUtils.copyProperties(noticeDO, vo);
            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(Long id) {
        SysNoticeDO existNotice = this.getById(id);
        if (existNotice == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "该公告不存在或已被删除");
        }
        
        // 使用 UpdateWrapper 直接更新 is_deleted 字段
        UpdateWrapper<SysNoticeDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                    .set("is_deleted", 1);
        boolean updated = this.update(updateWrapper);
        
        if (!updated) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "删除失败");
        }
        
        // 清理缓存
        String redisKey = RedisKeyConstants.NOTICE_DETAIL_KEY + id;
        stringRedisTemplate.delete(redisKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleTop(Long id) {
        SysNoticeDO existNotice = this.getById(id);
        if (existNotice == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "该公告不存在或已被删除");
        }

        // 切换置顶状态：0->1, 1->0
        Integer currentTop = existNotice.getIsTop() == null ? 0 : existNotice.getIsTop();
        existNotice.setIsTop(currentTop == 0 ? 1 : 0);
        this.updateById(existNotice);
    }
}
