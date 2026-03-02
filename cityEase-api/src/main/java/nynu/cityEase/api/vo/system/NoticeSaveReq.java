package nynu.cityEase.api.vo.system;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("保存或发布公告请求")
public class NoticeSaveReq {

    @ApiModelProperty(value = "公告ID (传了表示修改，不传表示新增)")
    private Long id;

    @ApiModelProperty(value = "公告标题", required = true)
    private String noticeTitle;

    @ApiModelProperty(value = "公告类型: 1-通知, 2-活动, 3-提示", required = true)
    private Integer noticeType;

    @ApiModelProperty(value = "公告HTML富文本内容", required = true)
    private String noticeContent;

    @ApiModelProperty(value = "是否直接发布: true-直接发布, false-存为草稿", required = true)
    private Boolean isPublish;
}