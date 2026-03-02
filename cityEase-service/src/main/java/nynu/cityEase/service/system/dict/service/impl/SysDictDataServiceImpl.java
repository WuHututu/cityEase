package nynu.cityEase.service.system.dict.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import nynu.cityEase.service.system.dict.repository.entity.SysDictDataDO;
import nynu.cityEase.service.system.dict.repository.mapper.SysDictDataMapper;
import nynu.cityEase.service.system.dict.service.ISysDictDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictDataDO> implements ISysDictDataService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 字典缓存前缀
    private static final String DICT_CACHE_PREFIX = "sys:dict:";

    @Override
    public List<SysDictDataDO> getDictDataByType(String dictType) {
        String cacheKey = DICT_CACHE_PREFIX + dictType;
        
        // 1. 查缓存
        String jsonStr = redisTemplate.opsForValue().get(cacheKey);
        if (StrUtil.isNotBlank(jsonStr)) {
            return JSONUtil.toList(jsonStr, SysDictDataDO.class);
        }

        // 2. 查数据库 (只查状态为 1 正常的，并且按 sort 排序)
        List<SysDictDataDO> list = this.list(new LambdaQueryWrapper<SysDictDataDO>()
                .eq(SysDictDataDO::getDictType, dictType)
                .eq(SysDictDataDO::getStatus, 1)
                .orderByAsc(SysDictDataDO::getDictSort));

        // 3. 写缓存 (字典数据不常变，可以存久一点，比如不需要设置过期时间，后台修改字典时主动 delete 即可)
        if (CollUtil.isNotEmpty(list)) {
            redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(list));
        }

        return list;
    }
}