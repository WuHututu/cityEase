package nynu.cityEase.api.vo.pms;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/11
 * Time: 16:06
 * Description: TODO
 */
@Data
public class PublicAreaReq {
    /**
     * 父级ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 区域名称
     */
    @TableField("name")
    private String name;

    /**
     * 层级: 1-小区, 2-楼栋...
     */
    @TableField("level")
    private Integer level;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;
}
