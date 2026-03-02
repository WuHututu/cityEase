package nynu.cityEase.web.front.pms;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.pms.BatchCreateRoomReq;
import nynu.cityEase.service.pms.service.IPmsRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pms/room")
@Api(tags = "房产房间管理")
public class PmsRestController {

    @Autowired
    private IPmsRoomService roomService;

    @PostMapping("/batchCreate")
    @ApiOperation("一键批量生成房间")
    public ResVo<String> batchCreate(@RequestBody BatchCreateRoomReq req) {
        roomService.batchCreateRooms(req);
        return ResVo.ok("批量生成成功");
    }
}