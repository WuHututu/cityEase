package nynu.cityEase.api.vo.pms;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HouseVO {

    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("区域类型: 0-公共区域, 1-房屋")
    private Integer areaType;

    @ApiModelProperty("区域名称")
    private String areaName;

    @ApiModelProperty("详细地址")
    private String areaAddress;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}