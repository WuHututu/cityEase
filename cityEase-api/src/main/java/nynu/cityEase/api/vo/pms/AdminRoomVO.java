package nynu.cityEase.api.vo.pms;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("〖后台〗房屋列表项")
public class AdminRoomVO {

  @ApiModelProperty("ID")
  private Long id;

  @ApiModelProperty("所属区域ID")
  private Long areaId;

  @ApiModelProperty("所属区域名称（最后一级）")
  private String areaName;

  @ApiModelProperty("门牌号")
  private String roomNum;

  @ApiModelProperty("完整地址（如：CityEase花园-1号楼-1单元-1101）")
  private String fullAddress;

  @ApiModelProperty("创建时间")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createTime;
}
