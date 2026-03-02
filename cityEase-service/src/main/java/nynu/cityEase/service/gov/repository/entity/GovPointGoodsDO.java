package nynu.cityEase.service.gov.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nynu.cityEase.api.entity.BaseDO;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gov_point_goods")
public class GovPointGoodsDO extends BaseDO {
    /**
     * 商品名称 (如：5L金龙鱼食用油)
     */
    private String name;
    /**
     * 商品描述
     */
    private String description;
    /**
     * 商品图片URL
     */
    private String imageUrl;
    /**
     * 兑换所需积分
     */
    private Integer pointsPrice;
    /**
     * 剩余库存
     */
    private Integer stock;
    /**
     * 上架状态: 1-上架, 0-下架
     */
    private Integer status;
}