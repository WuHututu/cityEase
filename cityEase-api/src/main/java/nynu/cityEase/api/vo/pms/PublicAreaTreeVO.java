package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("区域树状结构")
public class PublicAreaTreeVO implements Serializable {
    
    @ApiModelProperty("区域ID")
    private Long id;

    @ApiModelProperty("父级ID")
    private Long parentId;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("层级")
    private Integer level;

    @ApiModelProperty("子节点列表")
    private List<PublicAreaTreeVO> children;
}