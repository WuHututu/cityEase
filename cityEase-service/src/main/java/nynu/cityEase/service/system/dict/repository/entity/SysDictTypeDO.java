package nynu.cityEase.service.system.dict.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nynu.cityEase.api.entity.BaseDO;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class SysDictTypeDO extends BaseDO {
    @TableField("dict_name")
    private String dictName;
    @TableField("dict_type")
    private String dictType;
    @TableField("status")
    private Integer status;
    @TableField("remark")
    private String remark;
}
