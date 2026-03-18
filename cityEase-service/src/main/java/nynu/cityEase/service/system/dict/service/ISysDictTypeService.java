package nynu.cityEase.service.system.dict.service;

import nynu.cityEase.service.system.dict.repository.entity.SysDictTypeDO;

import java.util.List;

public interface ISysDictTypeService {
    /**
     * 获取字典类型列表
     */
    List<SysDictTypeDO> list();

    /**
     * 根据 ID 获取字典类型
     */
    SysDictTypeDO getById(Long id);

    /**
     * 新增字典类型
     */
    boolean save(SysDictTypeDO entity);

    /**
     * 修改字典类型
     */
    boolean updateById(SysDictTypeDO entity);

    /**
     * 删除字典类型
     */
    boolean deleteById(Long id);
}
