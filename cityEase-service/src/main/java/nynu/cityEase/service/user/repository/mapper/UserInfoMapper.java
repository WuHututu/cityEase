package nynu.cityEase.service.user.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nynu.cityEase.api.vo.user.UserSimpleVO;
import nynu.cityEase.service.user.repository.entity.UserInfoDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/7
 * Time: 17:02
 * Description: TODO
 */
public interface UserInfoMapper extends BaseMapper<UserInfoDO> {

    List<UserSimpleVO> selectSimpleByUserIds(@Param("userIds") List<Long> userIds);
}