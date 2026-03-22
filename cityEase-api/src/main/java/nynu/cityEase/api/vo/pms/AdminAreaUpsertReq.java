package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@ApiModel("〖后台〗公共区域新增/编辑请求")
public class AdminAreaUpsertReq {

  @ApiModelProperty("ID（编辑时必传）")
  private Long id;

  @ApiModelProperty("父级ID（顶级可不传或传0）")
  @Min(value = 0, message = "父级ID不能小于0")
  private Long parentId;

  @ApiModelProperty("区域名称")
  @NotBlank(message = "区域名称不能为空")
  @Size(max = 50, message = "区域名称长度不能超过50")
  private String areaName;

  @ApiModelProperty("区域类型（前端占位字段，后端暂不入库）")
  private String areaType;

  @ApiModelProperty("详细地址（前端占位字段，后端暂不入库）")
  private String areaAddress;

  @ApiModelProperty("排序（不传默认0）")
  private Integer sort;
}
