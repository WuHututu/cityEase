package nynu.cityEase.service.system.dict.service;
import nynu.cityEase.service.system.dict.repository.entity.SysDictDataDO;

import java.util.List;

public interface ISysDictDataService {
    /**
     * 根据字典类型获取字典数据列表（带Redis缓存）
     */
    List<SysDictDataDO> getDictDataByType(String dictType);
}