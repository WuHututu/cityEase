package nynu.cityEase.api.vo.mall;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("积分商城-商品信息")
public class PointGoodsVO {

    @ApiModelProperty("商品ID")
    private Long id;

    @ApiModelProperty("商品名称")
    private String name;

    @ApiModelProperty("商品简介/描述")
    private String description;

    @ApiModelProperty("商品主图URL")
    private String imageUrl;

    @ApiModelProperty("兑换所需积分")
    private BigDecimal points;

    @ApiModelProperty("库存")
    private Integer stock;

    @ApiModelProperty("上架状态：1上架 0下架")
    private Integer status;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
