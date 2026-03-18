package nynu.cityEase.api.vo.gov;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分排行榜VO
 * Created: 2026/3/18
 */
@Data
public class PointRankingVO {

    @ApiModelProperty("排名")
    private Integer ranking;

    @ApiModelProperty("房屋ID")
    private Long roomId;

    @ApiModelProperty("房屋编号")
    private String roomNum;

    @ApiModelProperty("区域ID（楼栋）")
    private Long areaId;

    @ApiModelProperty("区域名称（楼栋名称）")
    private String areaName;

    @ApiModelProperty("积分余额")
    private Integer pointsBalance;

    @ApiModelProperty("排名变化（正数上升，负数下降，0不变）")
    private Integer rankingChange;

    @ApiModelProperty("最后更新时间")
    private LocalDateTime updateTime;
}
