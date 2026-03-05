package nynu.cityEase.api.vo.pms;

import lombok.Data;

@Data
public class RepairHandlerOrderQueryReq {
    private Integer pageNo = 1;
    private Integer pageSize = 10;

    /** 可选：按状态筛选（一般是 1处理中 / 2已处理） */
    private Integer status;
}
