package nynu.cityEase.api.vo.gov;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GovPointChangeReq {

    @ApiModelProperty(value = "房屋ID (给哪户加减积分)", required = true)
    private Long roomId;

    @ApiModelProperty(value = "变动额度 (必须是大于0的正整数)", required = true, example = "10")
    private Integer amount;

    @ApiModelProperty(value = "是否增加: true-增加(发放), false-扣减(惩罚/兑换)", required = true)
    private Boolean isAdd;

    @ApiModelProperty(value = "变动原因", required = true, example = "参与周末社区扫雪志愿活动")
    private String reason;
}