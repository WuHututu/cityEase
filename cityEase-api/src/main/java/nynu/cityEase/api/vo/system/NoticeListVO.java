package nynu.cityEase.api.vo.system;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
// 轻量级，不含正文
public class NoticeListVO {

    @ApiModelProperty("公告 ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("公告标题")
    private String noticeTitle;

    @ApiModelProperty("公告类型")
    private Integer noticeType;

    @ApiModelProperty("是否置顶：0-否，1-是")
    private Integer isTop;

    @ApiModelProperty("状态：0-草稿，1-已发布")
    private Integer status;

    @ApiModelProperty("发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}