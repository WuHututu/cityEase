package nynu.cityEase.service.pms.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import nynu.cityEase.service.pms.repository.entity.PublicAreaDO;
import nynu.cityEase.service.pms.repository.mapper.PublicAreaMapper;
import nynu.cityEase.service.pms.repository.mapper.RoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/11
 * Time: 14:47
 * Description: TODO
 */
@Repository
public class PmsDao extends ServiceImpl<PublicAreaMapper, PublicAreaDO> {
    @Resource
    private RoomMapper roomMapper;
    @Resource
    private PublicAreaMapper publicAreaMapper;

    public boolean hasChildren(Long parentId) {
        // SQL: SELECT count(*) FROM pms_public_area WHERE parent_id = ? AND is_deleted = 0
        long count = this.count(new LambdaQueryWrapper<PublicAreaDO>()
                .eq(PublicAreaDO::getParentId, parentId));
        return count > 0;
    }
}