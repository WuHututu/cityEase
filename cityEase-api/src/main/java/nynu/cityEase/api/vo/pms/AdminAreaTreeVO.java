package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 后台：公共区域树（字段名与前端对齐：areaName/children）。
 */
@Data
@ApiModel("〖后台〗公共区域树节点")
public class AdminAreaTreeVO implements Serializable {

  @ApiModelProperty("区域ID")
  private Long id;

  @ApiModelProperty("父级ID")
  private Long parentId;

  @ApiModelProperty("区域名称")
  private String areaName;

  @ApiModelProperty("层级")
  private Integer level;

  @ApiModelProperty("排序")
  private Integer sort;

  @ApiModelProperty("子节点")
  private List<AdminAreaTreeVO> children;

  @ApiModelProperty("区域类型（字典值）")
  private String areaType;

  @ApiModelProperty("完整地址（拼接后的）")
  private String fullAddress;
}
