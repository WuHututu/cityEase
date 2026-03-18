package nynu.cityEase.web.admin.mall;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.mall.PointGoodsPageReq;
import nynu.cityEase.api.vo.mall.PointGoodsSaveReq;
import nynu.cityEase.api.vo.mall.PointGoodsVO;
import nynu.cityEase.service.mall.service.IPointGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/mall/goods")
@Api(tags = "〖后台〗积分商城-商品管理")
public class AdminMallGoodsController {

    @Autowired
    private IPointGoodsService goodsService;

    @PostMapping("/page")
    @ApiOperation("商品分页列表")
    public ResVo<IPage<PointGoodsVO>> page(@RequestBody PointGoodsPageReq req) {
        return ResVo.ok(goodsService.page(req));
    }

    @GetMapping("/detail")
    @ApiOperation("商品详情")
    public ResVo<PointGoodsVO> detail(@RequestParam("id") Long id) {
        return ResVo.ok(goodsService.detail(id));
    }

    @PostMapping("/save")
    @ApiOperation("新增或修改商品")
    public ResVo<String> save(@RequestBody PointGoodsSaveReq req) {
        goodsService.saveOrUpdate(req);
        return ResVo.ok(req.getId() == null ? "新增成功" : "修改成功");
    }

    @PostMapping("/delete")
    @ApiOperation("删除商品")
    public ResVo<String> delete(@RequestParam("id") Long id) {
        goodsService.removeById(id);
        return ResVo.ok("删除成功");
    }

    @PostMapping("/status")
    @ApiOperation("上下架")
    public ResVo<String> changeStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        goodsService.changeStatus(id, status);
        return ResVo.ok(status != null && status == 1 ? "已上架" : "已下架");
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResVo<String> handleIllegalArgument(IllegalArgumentException e) {
        // 统一把 service 里抛出的校验信息转为 fail
        return ResVo.fail(StatusEnum.valueOf(e.getMessage()));
    }
}
