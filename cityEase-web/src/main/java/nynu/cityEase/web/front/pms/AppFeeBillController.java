package nynu.cityEase.web.front.pms;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.pms.FeeBillVO;
import nynu.cityEase.api.vo.pms.FeePayReq;
import nynu.cityEase.service.pms.service.IPmsFeeBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/pms/fee")
@Api(tags = "【App端】物业费账单与缴费")
public class AppFeeBillController {

    @Autowired
    private IPmsFeeBillService feeBillService;

    @GetMapping("/myBills")
    @ApiOperation("查询我的房屋账单列表")
    public ResVo<List<FeeBillVO>> getMyBills() {
        return ResVo.ok(feeBillService.getMyBills());
    }

    @PostMapping("/pay")
    @ApiOperation("模拟支付物业费")
    public ResVo<String> payBill(@RequestBody FeePayReq req) {
        feeBillService.payBill(req.getBillId());
        return ResVo.ok("支付成功！感谢您对物业工作的支持。");
    }
}