package nynu.cityEase.api.vo.gov;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 楼栋积分统计VO
 * Created: 2026/3/18
 */
@Data
public class BuildingPointStatsVO {

    @ApiModelProperty("区域ID（楼栋ID）")
    private Long areaId;

    @ApiModelProperty("区域名称（楼栋名称）")
    private String areaName;

    @ApiModelProperty("楼栋总积分")
    private Integer totalPoints;

    @ApiModelProperty("房屋数量")
    private Integer roomCount;

    @ApiModelProperty("平均积分")
    private Double avgPoints;

    @ApiModelProperty("最高积分")
    private Integer maxPoints;

    @ApiModelProperty("最低积分")
    private Integer minPoints;

    @ApiModelProperty("楼栋排名")
    private Integer buildingRanking;

    @ApiModelProperty("最后更新时间")
    private LocalDateTime updateTime;
}
