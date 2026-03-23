package nynu.cityEase.service.pms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
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
        return vo;
    }

    private void fillAuditInfo(BindAuditDO doObj) {
        long auditorId = StpUtil.getLoginIdAsLong();
        doObj.setAuditorId(auditorId);

        UserInfoDO auditorInfo = userDao.getByUserId(auditorId);
        if (auditorInfo == null) {
            doObj.setAuditorName(String.valueOf(auditorId));
        } else if (StrUtil.isNotBlank(auditorInfo.getRealName())) {
            doObj.setAuditorName(auditorInfo.getRealName());
        } else {
            doObj.setAuditorName(auditorInfo.getUsername());
        }

        doObj.setAuditTime(LocalDateTime.now());
    }
}
