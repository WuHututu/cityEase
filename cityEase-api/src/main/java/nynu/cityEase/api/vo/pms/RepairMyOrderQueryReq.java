package nynu.cityEase.api.vo.pms;

import lombok.Data;

@Data
public class RepairMyOrderQueryReq {
    private Integer pageNo = 1;
    private Integer pageSize = 10;

    /** 可选：按状态筛选 */
    private Integer status;

    /** 可选：按报修类型筛选 */
    private String repairType;
}
