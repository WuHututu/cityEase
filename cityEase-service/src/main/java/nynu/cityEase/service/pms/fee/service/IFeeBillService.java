package nynu.cityEase.service.pms.fee.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import nynu.cityEase.api.vo.pms.*;

public interface IFeeBillService {

    Page<FeeBillPageVO> page(FeeBillPageReq req);

    FeeBillDetailVO detail(Long id);

    void save(FeeBillUpsertReq req);

    void update(FeeBillUpsertReq req);

    void delete(Long id);

    /** 为所有房屋批量生成某月账单（已存在的不再生成） */
    int generate(FeeBillGenerateReq req);

    void markPaid(FeeBillMarkPaidReq req);

    void markUnpaid(Long id);
}
