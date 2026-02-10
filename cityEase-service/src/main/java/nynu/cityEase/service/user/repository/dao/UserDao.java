package nynu.cityEase.service.user.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import nynu.cityEase.api.enums.YesOrNoEnum;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.service.user.repository.entity.UserDO;
import nynu.cityEase.service.user.repository.entity.UserInfoDO;
import nynu.cityEase.service.user.repository.mapper.UserInfoMapper;
import nynu.cityEase.service.user.repository.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/7
 * Time: 15:49
 * Description: TODO
 */
@Repository
public class UserDao extends ServiceImpl<UserInfoMapper, UserInfoDO> {
    @Resource
    UserMapper userMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;

    public UserDO getUserByPhone(String phone) {
        LambdaQueryWrapper<UserDO> qw = Wrappers.lambdaQuery();
        qw.eq(UserDO::getPhone, phone)
                .eq(UserDO::getIsDeleted, YesOrNoEnum.NO.getCode())
                .last("limit 1");
        return userMapper.selectOne(qw);
    }

    public void saveUser(UserDO userDO){
        if (userDO.getId()==null){
            userMapper.insert(userDO);
        }else {
            userMapper.updateById(userDO);
        }
    }

    public void saveUserInfo(UserInfoDO userInfoDO){
        if (userInfoDO.getId()==null){
            userInfoMapper.insert(userInfoDO);
        }else {
            userInfoMapper.updateById(userInfoDO);
        }
    }
}
