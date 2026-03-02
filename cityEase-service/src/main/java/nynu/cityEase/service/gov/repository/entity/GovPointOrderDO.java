package nynu.cityEase.service.gov.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nynu.cityEase.api.entity.BaseDO;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gov_point_order")
public class GovPointOrderDO extends BaseDO {
    private Long goodsId;
    private String goodsName;
    private Long roomId;
    private Long userId;
    private Integer pointsPaid;
    private Integer status;
}