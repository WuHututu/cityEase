package nynu.cityEase.service.system.dict.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nynu.cityEase.api.entity.BaseDO;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictDataDO extends BaseDO {
    @TableField("dict_type")
    private String dictType;
    @TableField("dict_label")
    private String dictLabel;
    @TableField("dict_value")
    private String dictValue;
    @TableField("dict_sort")
    private Integer dictSort;
    @TableField("status")
    private Integer status;
    @TableField("remark")
    private String remark;
}