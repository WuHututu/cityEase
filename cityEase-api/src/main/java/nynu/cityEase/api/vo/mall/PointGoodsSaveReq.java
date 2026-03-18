package nynu.cityEase.api.vo.mall;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("积分商城-商品新增/修改")
public class PointGoodsSaveReq {

    @ApiModelProperty("商品ID：新增不传，修改必传")
    private Long id;

    @ApiModelProperty("商品名称")
    private String name;

    @ApiModelProperty("商品简介/描述")
    private String description;

    @ApiModelProperty("商品主图 URL")
    private String imageUrl;
    
    @ApiModelProperty("兑换所需积分")
    private Integer pointsPrice;
    
    @ApiModelProperty("库存")
    private Integer stock;

    @ApiModelProperty("上架状态：1上架 0下架")
    private Integer status;
}
