package nynu.cityEase.api.vo.pms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("〖后台〗房屋分页查询请求")
public class AdminRoomPageReq {

  @ApiModelProperty("页码，默认1")
  private Integer pageNo = 1;

  @ApiModelProperty("每页条数，默认10")
  private Integer pageSize = 10;

  @ApiModelProperty("区域ID（可选）")
  private Long areaId;

  @ApiModelProperty("关键字：门牌号模糊查询（可选）")
  private String keyword;

  public <T> Page<T> toPage() {
    return new Page<>(this.pageNo, this.pageSize);
  }
}
