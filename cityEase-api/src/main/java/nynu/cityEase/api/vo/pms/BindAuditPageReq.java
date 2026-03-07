package nynu.cityEase.api.vo.pms;

import lombok.Data;

@Data
public class BindAuditPageReq {
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String ownerName;
    private String phone;
    private String roomKeyword;
    private Integer status;
}
