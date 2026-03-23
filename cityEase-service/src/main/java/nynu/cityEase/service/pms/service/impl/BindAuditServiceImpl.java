package nynu.cityEase.service.pms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.pms.BindAuditDetailVO;
import nynu.cityEase.api.vo.pms.BindAuditPageReq;
import nynu.cityEase.api.vo.pms.BindAuditPageVO;
import nynu.cityEase.service.pms.service.IBindAuditService;
import nynu.cityEase.service.pms.service.entity.BindAuditDO;
import nynu.cityEase.service.pms.service.mapper.BindAuditMapper;
import nynu.cityEase.service.user.repository.dao.UserDao;
import nynu.cityEase.service.user.repository.entity.UserDO;
import nynu.cityEase.service.user.repository.entity.UserInfoDO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BindAuditServiceImpl extends ServiceImpl<BindAuditMapper, BindAuditDO> implements IBindAuditService {

    @Autowired
    private UserDao userDao;

    @Override
    public Page<BindAuditPageVO> getBindPage(BindAuditPageReq req) {
        LambdaQueryWrapper<BindAuditDO> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(req.getOwnerName())) {
            wrapper.like(BindAuditDO::getOwnerName, req.getOwnerName());
        }
        if (StrUtil.isNotBlank(req.getPhone())) {
            wrapper.eq(BindAuditDO::getPhone, req.getPhone());
        }
        if (StrUtil.isNotBlank(req.getRoomKeyword())) {
            wrapper.like(BindAuditDO::getRoomInfo, req.getRoomKeyword());
        }
        if (req.getStatus() != null) {
            wrapper.eq(BindAuditDO::getStatus, req.getStatus());
        }
        wrapper.orderByDesc(BindAuditDO::getApplyTime);

        Page<BindAuditDO> page = this.page(new Page<>(req.getPageNo(), req.getPageSize()), wrapper);
        Page<BindAuditPageVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<BindAuditPageVO> list = new ArrayList<>();
        for (BindAuditDO doObj : page.getRecords()) {
            BindAuditPageVO vo = new BindAuditPageVO();
            BeanUtils.copyProperties(doObj, vo);
            vo.setAttachmentsList(parseAttachments(doObj.getAttachments()));
            list.add(vo);
        }
        voPage.setRecords(list);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        BindAuditDO doObj = this.getById(id);
        if (doObj == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "绑定申请不存在");
        }
        doObj.setStatus(1);
        fillAuditInfo(doObj);
        this.updateById(doObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String remark) {
        BindAuditDO doObj = this.getById(id);
        if (doObj == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "绑定申请不存在");
        }
        doObj.setStatus(2);
        doObj.setRemark(remark);
        fillAuditInfo(doObj);
        this.updateById(doObj);
    }

    @Override
    public BindAuditDetailVO getBindDetail(Long id) {
        BindAuditDO doObj = this.getById(id);
        if (doObj == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "绑定申请不存在");
        }
        BindAuditDetailVO vo = new BindAuditDetailVO();
        BeanUtils.copyProperties(doObj, vo);
        vo.setAttachmentsList(parseAttachments(doObj.getAttachments()));
        return vo;
    }

    private List<String> parseAttachments(String attachments) {
        if (StrUtil.isBlank(attachments)) {
            return new ArrayList<>();
        }
        try {
            return JSONUtil.toList(attachments, String.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void fillAuditInfo(BindAuditDO doObj) {
        long auditorId = StpUtil.getLoginIdAsLong();
        doObj.setAuditorId(auditorId);

        UserInfoDO auditorInfo = userDao.getByUserId(auditorId);
        UserDO auditorUser = userDao.getUserById(auditorId);
        if (auditorInfo != null && StrUtil.isNotBlank(auditorInfo.getRealName())) {
            doObj.setAuditorName(auditorInfo.getRealName());
        } else if (auditorInfo != null && StrUtil.isNotBlank(auditorInfo.getUsername())) {
            doObj.setAuditorName(auditorInfo.getUsername());
        } else if (auditorUser != null && StrUtil.isNotBlank(auditorUser.getPhone())) {
            doObj.setAuditorName(auditorUser.getPhone());
        } else {
            doObj.setAuditorName(String.valueOf(auditorId));
        }

        doObj.setAuditTime(LocalDateTime.now());
    }
}
