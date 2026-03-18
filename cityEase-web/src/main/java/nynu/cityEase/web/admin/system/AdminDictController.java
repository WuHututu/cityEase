package nynu.cityEase.web.admin.system;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.service.system.dict.repository.entity.SysDictDataDO;
import nynu.cityEase.service.system.dict.repository.entity.SysDictTypeDO;
import nynu.cityEase.service.system.dict.service.ISysDictDataService;
import nynu.cityEase.service.system.dict.service.ISysDictTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理 Controller
 */
@Api(tags = "字典管理")
@RestController
@RequestMapping("/admin/system/dict")
public class AdminDictController {

    @Autowired
    private ISysDictTypeService dictTypeService;

    @Autowired
    private ISysDictDataService dictDataService;

    // ==================== 字典类型管理 ====================

    @GetMapping("/type/list")
    @ApiOperation("获取字典类型列表")
    public ResVo<List<SysDictTypeDO>> listTypes() {
        return ResVo.ok(dictTypeService.list());
    }

    @GetMapping("/type/{id}")
    @ApiOperation("根据 ID 获取字典类型")
    public ResVo<SysDictTypeDO> getType(@PathVariable Long id) {
        return ResVo.ok(dictTypeService.getById(id));
    }

    @PostMapping("/type")
    @ApiOperation("新增字典类型")
    public ResVo<Boolean> saveType(@RequestBody SysDictTypeDO entity) {
        return ResVo.ok(dictTypeService.save(entity));
    }

    @PutMapping("/type")
    @ApiOperation("修改字典类型")
    public ResVo<Boolean> updateType(@RequestBody SysDictTypeDO entity) {
        return ResVo.ok(dictTypeService.updateById(entity));
    }

    @DeleteMapping("/type/{id}")
    @ApiOperation("删除字典类型")
    public ResVo<Boolean> deleteType(@PathVariable Long id) {
        return ResVo.ok(dictTypeService.deleteById(id));
    }

    // ==================== 字典数据管理 ====================

    @GetMapping("/data/list")
    @ApiOperation("获取字典数据列表（带筛选）")
    public ResVo<List<SysDictDataDO>> listData(
            @RequestParam(required = false) String dictType,
            @RequestParam(required = false) String label) {
        return ResVo.ok(dictDataService.list(dictType, label));
    }

    @GetMapping("/data/byType/{dictType}")
    @ApiOperation("根据字典类型获取字典数据（前端下拉框专用，带缓存）")
    public ResVo<List<SysDictDataDO>> getDataByType(@PathVariable String dictType) {
        return ResVo.ok(dictDataService.getDictDataByType(dictType));
    }

    @GetMapping("/data/{id}")
    @ApiOperation("根据 ID 获取字典数据")
    public ResVo<SysDictDataDO> getData(@PathVariable Long id) {
        return ResVo.ok(dictDataService.getById(id));
    }

    @PostMapping("/data")
    @ApiOperation("新增字典数据")
    public ResVo<Boolean> saveData(@RequestBody SysDictDataDO entity) {
        return ResVo.ok(dictDataService.save(entity));
    }

    @PutMapping("/data")
    @ApiOperation("修改字典数据")
    public ResVo<Boolean> updateData(@RequestBody SysDictDataDO entity) {
        return ResVo.ok(dictDataService.updateById(entity));
    }

    @DeleteMapping("/data/{id}")
    @ApiOperation("删除字典数据")
    public ResVo<Boolean> deleteData(@PathVariable Long id) {
        return ResVo.ok(dictDataService.deleteById(id));
    }
}
