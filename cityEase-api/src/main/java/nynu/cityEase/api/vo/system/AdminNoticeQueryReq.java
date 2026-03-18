package nynu.cityEase.api.vo.system;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 管理端：分页查询公告列表请求
 */
@Data
@ApiModel("管理端公告分页查询请求")
public class AdminNoticeQueryReq {

    @ApiModelProperty("页码")
    private Integer pageNo = 1;

    @ApiModelProperty("每页大小")
    private Integer pageSize = 10;

    @ApiModelProperty("公告类型：1-通知，2-公告，3-活动")
    private Integer noticeType;

    @ApiModelProperty("状态：0-草稿，1-已发布")
    private Integer status;

    @ApiModelProperty("标题关键字")
    private String keyword;
}
