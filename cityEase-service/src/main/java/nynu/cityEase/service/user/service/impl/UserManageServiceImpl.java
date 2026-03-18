package nynu.cityEase.service.user.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import nynu.cityEase.api.exception.CommunityException;
import nynu.cityEase.api.vo.user.UserPageReq;
import nynu.cityEase.api.vo.user.UserPageVO;
import nynu.cityEase.api.vo.user.UserSaveReq;
import nynu.cityEase.service.user.repository.entity.UserDO;
import nynu.cityEase.service.user.repository.entity.UserInfoDO;
import nynu.cityEase.service.user.repository.mapper.UserMapper;
import nynu.cityEase.service.user.repository.mapper.UserInfoMapper;
import nynu.cityEase.service.user.service.IUserManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserManageServiceImpl implements IUserManageService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public IPage<UserPageVO> page(UserPageReq req) {
        Page<UserDO> page = new Page<>(req.getCurrent(), req.getSize());

        // 先查询用户表
        LambdaQueryWrapper<UserDO> qw = new LambdaQueryWrapper<>();
        if (req.getUserRole() != null) {
            qw.eq(UserDO::getUserRole, req.getUserRole());
        }
        if (req.getIsDisabled() != null) {
            qw.eq(UserDO::getStatus, req.getIsDisabled());
        }
        // 关键词搜索：用户名、真实姓名、手机号
        if (!StrUtil.isBlank(req.getKeyword())) {
            String keyword = req.getKeyword().trim();
            // 先查询匹配的用户 ID
            LambdaQueryWrapper<UserInfoDO> infoQw = new LambdaQueryWrapper<>();
            infoQw.select(UserInfoDO::getUserId)
                    .and(wrapper -> wrapper
                            .like(UserInfoDO::getUsername, keyword)
                            .or()
                            .like(UserInfoDO::getRealName, keyword));
            List<Long> matchedUserIds = userInfoMapper.selectList(infoQw)
                    .stream()
                    .map(UserInfoDO::getUserId)
                    .collect(java.util.stream.Collectors.toList());
            
            // 在用户表中搜索：手机号 OR 匹配的用户 ID
            qw.and(wrapper -> wrapper
                    .like(UserDO::getPhone, keyword)
                    .or()
                    .in(UserDO::getId, matchedUserIds.isEmpty() ? java.util.Collections.singletonList(-1L) : matchedUserIds));
        }
        qw.orderByDesc(UserDO::getCreateTime);

        IPage<UserDO> userPage = userMapper.selectPage(page, qw);

        // 转换为 VO（从 UserInfoDO 中获取 username 等信息）
        Page<UserPageVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(this::convertToVO).toList());
        return voPage;
    }

    private UserPageVO convertToVO(UserDO user) {
        UserPageVO vo = new UserPageVO();
        vo.setUserId(user.getId());
        vo.setUserRole(user.getUserRole());
        vo.setIsDisabled(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        vo.setPhone(user.getPhone()); // 设置手机号

        // 查询用户详情表获取更多信息（包括 username）
        UserInfoDO info = userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfoDO>().eq(UserInfoDO::getUserId, user.getId())
        );
        if (info != null) {
            vo.setUsername(info.getUsername());
            vo.setRealName(info.getRealName());
            vo.setAvatar(info.getAvatar());
            vo.setGender(info.getGender());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(UserSaveReq req) {
        if (req.getUserId() != null) {
            // 更新用户
            updateUser(req);
        } else {
            // 新增用户
            saveUser(req);
        }
    }

    /**
     * 新增用户
     */
    private void saveUser(UserSaveReq req) {
        // 1. 检查用户名是否已存在（从 UserInfoDO 中查询）
        UserInfoDO existInfo = userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfoDO>().eq(UserInfoDO::getUsername, req.getUsername())
        );
        if (existInfo != null) {
            throw new CommunityException(400, "用户名已存在");
        }

        // 2. 创建用户账号
        UserDO user = new UserDO();
        user.setPhone(req.getPhone());
        
        // 密码加密
        String encodedPassword = BCrypt.hashpw(req.getPassword(), BCrypt.gensalt());
        user.setPassword(encodedPassword);
        
        user.setStatus(0); // 0-正常
        user.setLoginType(1); // 1-手机号密码登录
        user.setUserRole(req.getUserRole());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        userMapper.insert(user);

        // 3. 创建用户详情
        UserInfoDO info = new UserInfoDO();
        info.setUserId(user.getId());
        info.setUsername(req.getUsername());
        info.setRealName(req.getRealName());
        info.setAvatar(req.getAvatar()); // 设置头像
        info.setGender(req.getGender());
        info.setCreateTime(LocalDateTime.now());
        info.setUpdateTime(LocalDateTime.now());
        
        userInfoMapper.insert(info);
    }

    /**
     * 更新用户
     */
    private void updateUser(UserSaveReq req) {
        // 更新用户表
        UserDO user = userMapper.selectById(req.getUserId());
        if (user == null) {
            throw new CommunityException(400, "用户不存在");
        }

        user.setPhone(req.getPhone());
        if (req.getUserRole() != null) {
            user.setUserRole(req.getUserRole());
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 更新用户详情表
        UserInfoDO info = userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfoDO>().eq(UserInfoDO::getUserId, req.getUserId())
        );
        if (info == null) {
            // 如果详情表没有记录，创建一条
            info = new UserInfoDO();
            info.setUserId(req.getUserId());
            info.setUsername(req.getUsername());
            info.setRealName(req.getRealName());
            info.setAvatar(req.getAvatar()); // 设置头像
            info.setGender(req.getGender());
            info.setCreateTime(LocalDateTime.now());
            info.setUpdateTime(LocalDateTime.now());
            userInfoMapper.insert(info);
        } else {
            info.setRealName(req.getRealName());
            info.setGender(req.getGender());
            info.setUsername(req.getUsername());
            info.setAvatar(req.getAvatar()); // 更新头像
            info.setUpdateTime(LocalDateTime.now());
            userInfoMapper.updateById(info);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableUser(Long userId, Integer disable) {
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new CommunityException(400, "用户不存在");
        }

        // 使用 status 字段：0-正常，1-禁用
        user.setStatus(disable);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId) {
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new CommunityException(400, "用户不存在");
        }

        // 重置为默认密码：123456
        String defaultPassword = "123456";
        String encodedPassword = BCrypt.hashpw(defaultPassword, BCrypt.gensalt());
        user.setPassword(encodedPassword);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }
}
