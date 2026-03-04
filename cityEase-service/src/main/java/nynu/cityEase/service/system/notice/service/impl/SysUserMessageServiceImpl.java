package nynu.cityEase.service.system.notice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import nynu.cityEase.service.system.dict.repository.entity.SysUserMessageDO;
import nynu.cityEase.service.system.dict.repository.mapper.SysUserMessageMapper;
import nynu.cityEase.service.system.notice.service.ISysUserMessageService;
import org.springframework.stereotype.Service;

@Service
public class SysUserMessageServiceImpl extends ServiceImpl<SysUserMessageMapper, SysUserMessageDO> implements ISysUserMessageService {
}