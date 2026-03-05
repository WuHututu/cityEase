package nynu.cityEase.service.pms.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nynu.cityEase.service.pms.repository.entity.RepairOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RepairOrderMapper extends BaseMapper<RepairOrderDO> {

    @Select("""
    SELECT DISTINCT handler_id
    FROM pms_repair_order
    WHERE handler_id IS NOT NULL
""")
    List<Long> selectDistinctHandlerIds();
}