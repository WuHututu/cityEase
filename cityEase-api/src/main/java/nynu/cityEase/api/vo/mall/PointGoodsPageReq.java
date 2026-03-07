package nynu.cityEase.api.vo.mall;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("积分商城-商品分页查询")
public class PointGoodsPageReq {

    @ApiModelProperty("当前页，从1开始")
    private Long current = 1L;

    @ApiModelProperty("每页大小")
    private Long size = 10L;

    @ApiModelProperty("关键字(商品名)")
    private String keyword;

    @ApiModelProperty("上架状态：1上架 0下架；为空=全部")
    private Integer status;
}
