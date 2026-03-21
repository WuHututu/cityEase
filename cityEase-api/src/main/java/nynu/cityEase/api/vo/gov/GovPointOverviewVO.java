package nynu.cityEase.api.vo.gov;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("积分总览核心指标")
@Data
public class GovPointOverviewVO {

    @ApiModelProperty("累计积分发放")
    private Integer totalGranted;

    @ApiModelProperty("累计积分消耗")
    private Integer totalConsumed;

    @ApiModelProperty("累计参与用户数")
    private Long participantUsers;

    @ApiModelProperty("今日净积分变动（发放-消耗）")
    private Integer todayNetChange;
}
