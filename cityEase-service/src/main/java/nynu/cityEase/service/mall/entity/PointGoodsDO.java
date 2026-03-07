package nynu.cityEase.service.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("gov_point_goods")
public class PointGoodsDO {

    @TableId(type = IdType.ASSIGN_ID)
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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
