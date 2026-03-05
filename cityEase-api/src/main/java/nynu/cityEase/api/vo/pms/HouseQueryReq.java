package nynu.cityEase.api.vo.pms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class HouseQueryReq {

    @ApiModelProperty("页码，默认1")
    private Integer pageNo = 1;

    @ApiModelProperty("每页条数，默认10")
    private Integer pageSize = 10;

    @ApiModelProperty("区域类型: 0-公共区域, 1-房屋 (不传则查全部)")
    private Integer areaType;

    @ApiModelProperty("区域名称关键字")
    private String areaName;
    
    public <T> Page<T> toPage() {
        return new Page<>(this.pageNo, this.pageSize);
    }
}