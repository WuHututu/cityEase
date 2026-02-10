package nynu.cityEase.api.vo.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import nynu.cityEase.api.entity.BaseDTO;

import java.util.Date;

/**
 * @author YiHui
 * @date 2022/8/15
 */
@Data
@ApiModel("用户基础实体对象")
@Accessors(chain = true)
public class BaseUserInfoDTO extends BaseDTO {

}
