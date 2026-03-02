package nynu.cityEase.api.vo.system;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AppNoticeQueryReq {

    @ApiModelProperty("页码，默认1")
    private Integer pageNo = 1;

    @ApiModelProperty("每页条数，默认10")
    private Integer pageSize = 10;

    @ApiModelProperty("公告类型: 1-通知, 2-活动, 3-提示 (不传则查全部)")
    private Integer noticeType;
}