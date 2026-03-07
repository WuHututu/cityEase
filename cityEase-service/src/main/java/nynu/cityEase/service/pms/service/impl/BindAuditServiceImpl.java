package nynu.cityEase.service.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.pms.BindAuditDetailVO;
import nynu.cityEase.api.vo.pms.BindAuditPageVO;
import nynu.cityEase.api.vo.pms.BindAuditPageReq;
import nynu.cityEase.service.pms.service.IBindAuditService;
import nynu.cityEase.service.pms.service.entity.BindAuditDO;
import nynu.cityEase.service.pms.service.mapper.BindAuditMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class BindAuditServiceImpl extends ServiceImpl<BindAuditMapper, BindAuditDO> implements IBindAuditService {

    @Override
    public Page<BindAuditPageVO> getBindPage(BindAuditPageReq req) {
        LambdaQueryWrapper<BindAuditDO> wrapper = new LambdaQueryWrapper<>();
        if(req.getOwnerName()!=null && !req.getOwnerName().isEmpty()){
            wrapper.like(BindAuditDO::getOwnerName,req.getOwnerName());
        }
        if(req.getPhone()!=null && !req.getPhone().isEmpty()){
            wrapper.eq(BindAuditDO::getPhone,req.getPhone());
        }
        if(req.getRoomKeyword()!=null && !req.getRoomKeyword().isEmpty()){
            wrapper.like(BindAuditDO::getRoomInfo,req.getRoomKeyword());
        }
        if(req.getStatus()!=null){
            wrapper.eq(BindAuditDO::getStatus,req.getStatus());
        }
        wrapper.orderByDesc(BindAuditDO::getApplyTime);

        Page<BindAuditDO> page = this.page(new Page<>(req.getPageNo(),req.getPageSize()),wrapper);
        Page<BindAuditPageVO> voPage = new Page<>(page.getCurrent(),page.getSize(),page.getTotal());
        List<BindAuditPageVO> list = new ArrayList<>();
        for(BindAuditDO doObj: page.getRecords()){
            BindAuditPageVO vo = new BindAuditPageVO();
            BeanUtils.copyProperties(doObj,vo);
            list.add(vo);
        }
        voPage.setRecords(list);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        BindAuditDO doObj = this.getById(id);
        if(doObj==null) throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED,"绑定申请不存在");
        doObj.setStatus(1);
        this.updateById(doObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String remark) {
        BindAuditDO doObj = this.getById(id);
        if(doObj==null) throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED,"绑定申请不存在");
        doObj.setStatus(2);
        doObj.setRemark(remark);
        this.updateById(doObj);
    }

    @Override
    public BindAuditDetailVO getBindDetail(Long id) {
        BindAuditDO doObj = this.getById(id);
        if(doObj==null) throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED,"绑定申请不存在");
        BindAuditDetailVO vo = new BindAuditDetailVO();
        BeanUtils.copyProperties(doObj,vo);
        return vo;
    }
}
