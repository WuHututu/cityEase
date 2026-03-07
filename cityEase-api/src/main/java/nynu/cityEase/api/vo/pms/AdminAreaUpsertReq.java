package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 后台：公共区域 新增/编辑 请求
 *
 * 说明：为了兼容管理端现有表单字段（areaName/areaType/areaAddress），这里只强约束 areaName/parentId。
 * areaType/areaAddress 暂不入库（当前表结构 pms_public_area 仅有 name/parent_id/level/sort）。
 */
@Data
@ApiModel("〖后台〗公共区域新增/编辑请求")
public class AdminAreaUpsertReq {

  @ApiModelProperty("ID（编辑时必传）")
  private Long id;

  @ApiModelProperty("父级ID（顶级可不传或传0）")
  private Long parentId;

  @ApiModelProperty("区域名称")
  private String areaName;

  @ApiModelProperty("区域类型（前端占位字段，后端暂不入库）")
  private String areaType;

  @ApiModelProperty("详细地址（前端占位字段，后端暂不入库）")
  private String areaAddress;

  @ApiModelProperty("排序（不传默认0）")
  private Integer sort;
}
