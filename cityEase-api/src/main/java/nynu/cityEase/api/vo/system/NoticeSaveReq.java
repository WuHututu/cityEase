package nynu.cityEase.api.vo.system;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Notice save request")
public class NoticeSaveReq {

    @ApiModelProperty(value = "Notice ID. Null means create, otherwise update.")
    private Long id;

    @ApiModelProperty(value = "Notice title", required = true)
    private String noticeTitle;

    @ApiModelProperty(value = "Notice type: 1-notice, 2-activity, 3-tip", required = true)
    private Integer noticeType;

    @ApiModelProperty(value = "Notice content in HTML", required = true)
    private String noticeContent;

    @ApiModelProperty(value = "Cover image URL")
    private String coverImage;

    @ApiModelProperty(value = "Whether to publish immediately", required = true)
    private Boolean isPublish;
}
