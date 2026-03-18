package nynu.cityEase.service.system.dict.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import nynu.cityEase.service.system.dict.repository.entity.SysDictDataDO;
import nynu.cityEase.service.system.dict.repository.entity.SysDictTypeDO;
import nynu.cityEase.service.system.dict.repository.mapper.SysDictTypeMapper;
import nynu.cityEase.service.system.dict.service.ISysDictTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictTypeDO> implements ISysDictTypeService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 字典类型缓存前缀
    private static final String DICT_TYPE_CACHE_PREFIX = "sys:dict:type:";

    @Override
    public List<SysDictTypeDO> list() {
        return this.list(new LambdaQueryWrapper<SysDictTypeDO>()
                .eq(SysDictTypeDO::getStatus, 1)
                .orderByAsc(SysDictTypeDO::getId));
    }

    @Override
    public SysDictTypeDO getById(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean save(SysDictTypeDO entity) {
        boolean result = super.save(entity);
        // 清除缓存
        clearCache();
        return result;
    }

    @Override
    public boolean updateById(SysDictTypeDO entity) {
        boolean result = super.updateById(entity);
        // 清除缓存
        clearCache();
        return result;
    }

    @Override
    public boolean deleteById(Long id) {
        boolean result = super.removeById(id);
        // 清除缓存
        clearCache();
        return result;
    }

    /**
     * 清除字典类型缓存
     */
    private void clearCache() {
        // 简单粗暴：删除所有字典类型相关缓存（实际可优化为只删当前修改的）
        redisTemplate.delete(DICT_TYPE_CACHE_PREFIX + "*");
    }
}
