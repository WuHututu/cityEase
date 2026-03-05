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


    @Select("""
                SELECT u.user_id AS userId,
                       CASE
                         WHEN u.real_name IS NOT NULL AND u.real_name <> '' THEN u.real_name
                         WHEN u.username IS NOT NULL AND u.username <> '' THEN u.username
                         ELSE CONCAT('用户', u.user_id)
                       END AS name
                FROM sys_user_info u
                WHERE u.is_deleted = 0
                  AND u.user_role <> 1
                  AND u.user_id NOT IN (
                      SELECT r.user_id
                      FROM pms_user_room_rel r
                      WHERE r.is_deleted = 0
                  )
                ORDER BY u.update_time DESC
                LIMIT 50
            """)
    List<UserSimpleVO> selectNoRoomNonAdminHandlers();

}