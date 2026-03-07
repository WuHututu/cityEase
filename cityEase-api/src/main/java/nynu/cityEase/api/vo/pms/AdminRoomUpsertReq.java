package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 后台：房屋 新增/编辑 请求
 */
@Data
@ApiModel("〖后台〗房屋新增/编辑请求")
public class AdminRoomUpsertReq {

  @ApiModelProperty("ID（编辑时必传）")
  private Long id;

  @ApiModelProperty("所属区域ID（必传，通常是单元/楼栋下的最底层区域）")
  private Long areaId;

  @ApiModelProperty("门牌号（如 1101 / 2001）")
  private String roomNum;

  @ApiModelProperty("备注（前端占位字段，后端暂不入库）")
  private String remark;
}
