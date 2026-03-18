package nynu.cityEase.api.vo.system;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DashboardMetricsVO {

    @ApiModelProperty("系统录入的房屋总数")
    private Long totalRooms;

    @ApiModelProperty("已认证的业主/家属总人数")
    private Long authenticatedUsers;

    @ApiModelProperty("待派发的报修工单数 (红点预警)")
    private Long pendingRepairs;

    @ApiModelProperty("处理中的报修工单数")
    private Long processingRepairs;

    @ApiModelProperty("已发布的系统公告总数")
    private Long publishedNotices;

    @ApiModelProperty("今日新增公告数")
    private Long todayNotices;

    @ApiModelProperty("本周新增公告数")
    private Long weekNotices;

    @ApiModelProperty("置顶公告数")
    private Long topNotices;
}