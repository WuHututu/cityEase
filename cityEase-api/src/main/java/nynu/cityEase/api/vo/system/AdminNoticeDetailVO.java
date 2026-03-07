package nynu.cityEase.api.vo.system;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminNoticeDetailVO {

    @ApiModelProperty("公告ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("公告标题")
    private String noticeTitle;

    @ApiModelProperty("公告类型")
    private Integer noticeType;

    @ApiModelProperty("公告正文")
    private String noticeContent;

    @ApiModelProperty("状态: 0-草稿, 1-已发布")
    private Integer status;

    @ApiModelProperty("创建人ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long creatorId;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
