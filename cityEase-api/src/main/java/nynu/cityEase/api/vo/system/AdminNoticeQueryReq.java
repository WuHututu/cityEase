package nynu.cityEase.api.vo.system;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("后台公告分页查询请求")
public class AdminNoticeQueryReq {

    @ApiModelProperty("页码")
    private Integer pageNo = 1;

    @ApiModelProperty("每页条数")
    private Integer pageSize = 10;

    @ApiModelProperty("公告类型: 1-通知, 2-活动, 3-提示")
    private Integer noticeType;

    @ApiModelProperty("状态: 0-草稿, 1-已发布")
    private Integer status;

    @ApiModelProperty("关键字: 标题模糊匹配")
    private String keyword;
}
