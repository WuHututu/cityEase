package nynu.cityEase.service.system.dict.service;
import nynu.cityEase.service.system.dict.repository.entity.SysDictDataDO;

import java.util.List;

public interface ISysDictDataService {
    /**
     * 根据字典类型获取字典数据列表（带 Redis 缓存）
     */
    List<SysDictDataDO> getDictDataByType(String dictType);

    /**
     * 获取字典数据列表（分页查询用）
     */
    List<SysDictDataDO> list(String dictType, String label);

    /**
     * 根据 ID 获取字典数据
     */
    SysDictDataDO getById(Long id);

    /**
     * 新增字典数据
     */
    boolean save(SysDictDataDO entity);

    /**
     * 修改字典数据
     */
    boolean updateById(SysDictDataDO entity);

    /**
     * 删除字典数据
     */
    boolean deleteById(Long id);
}