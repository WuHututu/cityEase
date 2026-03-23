package nynu.cityEase.service.system.notice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNoticeDO> implements ISysNoticeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateNotice(NoticeSaveReq req) {
        long adminId = StpUtil.getLoginIdAsLong();
        int targetStatus = Boolean.TRUE.equals(req.getIsPublish()) ? 1 : 0;

        if (req.getId() == null) {
            SysNoticeDO noticeDO = new SysNoticeDO();
            BeanUtils.copyProperties(req, noticeDO);
            noticeDO.setStatus(targetStatus);
            noticeDO.setCreatorId(adminId);
            this.save(noticeDO);
            return;
        }

        SysNoticeDO existNotice = this.getById(req.getId());
        if (existNotice == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "公告不存在或已删除");
        }

        if (req.getNoticeTitle() != null) {
            existNotice.setNoticeTitle(req.getNoticeTitle());
        }
        if (req.getNoticeType() != null) {
            existNotice.setNoticeType(req.getNoticeType());
        }
        if (req.getNoticeContent() != null) {
            existNotice.setNoticeContent(req.getNoticeContent());
        }
        if (req.getCoverImage() != null) {
            existNotice.setCoverImage(req.getCoverImage());
        }
        existNotice.setStatus(targetStatus);

        this.updateById(existNotice);
        stringRedisTemplate.delete(RedisKeyConstants.NOTICE_DETAIL_KEY + req.getId());
    }

    @Override
    public Page<NoticeListVO> getPublishedNotices(AppNoticeQueryReq req) {
        LambdaQueryWrapper<SysNoticeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNoticeDO::getStatus, 1);

        if (req.getNoticeType() != null) {
            wrapper.eq(SysNoticeDO::getNoticeType, req.getNoticeType());
        }

        wrapper.orderByDesc(SysNoticeDO::getCreateTime);
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
        String redisKey = RedisKeyConstants.NOTICE_DETAIL_KEY + id;
        String cacheJson = stringRedisTemplate.opsForValue().get(redisKey);
        if (StrUtil.isNotBlank(cacheJson)) {
            return JSONUtil.toBean(cacheJson, NoticeDetailVO.class);
        }

        SysNoticeDO noticeDO = this.getById(id);
        if (noticeDO == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "公告不存在");
        }

        NoticeDetailVO vo = new NoticeDetailVO();
        BeanUtils.copyProperties(noticeDO, vo);
        stringRedisTemplate.opsForValue().set(redisKey, JSONUtil.toJsonStr(vo), 24, TimeUnit.HOURS);
        return vo;
    }

    @Override
    public Page<NoticeListVO> page(AdminNoticeQueryReq req) {
        LambdaQueryWrapper<SysNoticeDO> wrapper = new LambdaQueryWrapper<>();

        if (req.getNoticeType() != null) {
            wrapper.eq(SysNoticeDO::getNoticeType, req.getNoticeType());
        }
        if (req.getStatus() != null) {
            wrapper.eq(SysNoticeDO::getStatus, req.getStatus());
        }
        if (StrUtil.isNotBlank(req.getKeyword())) {
            wrapper.like(SysNoticeDO::getNoticeTitle, req.getKeyword().trim());
        }

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
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "公告不存在或已删除");
        }

        UpdateWrapper<SysNoticeDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).set("is_deleted", 1);
        boolean updated = this.update(updateWrapper);
        if (!updated) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "删除失败");
        }

        stringRedisTemplate.delete(RedisKeyConstants.NOTICE_DETAIL_KEY + id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleTop(Long id) {
        SysNoticeDO existNotice = this.getById(id);
        if (existNotice == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "公告不存在或已删除");
        }

        Integer currentTop = existNotice.getIsTop() == null ? 0 : existNotice.getIsTop();
        existNotice.setIsTop(currentTop == 0 ? 1 : 0);
        this.updateById(existNotice);
    }
}
