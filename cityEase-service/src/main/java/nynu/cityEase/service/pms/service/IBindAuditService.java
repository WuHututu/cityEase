package nynu.cityEase.service.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import nynu.cityEase.api.vo.pms.BindAuditDetailVO;
import nynu.cityEase.api.vo.pms.BindAuditPageVO;
import nynu.cityEase.api.vo.pms.BindAuditPageReq;

public interface IBindAuditService {
    Page<BindAuditPageVO> getBindPage(BindAuditPageReq req);
    void approve(Long id);
    void reject(Long id,String remark);
    BindAuditDetailVO getBindDetail(Long id);
}
