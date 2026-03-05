package nynu.cityEase.core.handler;

import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nynu.cityEase.service.user.repository.entity.IpInfo;

/**
 * 自定义IP信息类型处理器，用于处理数据库中可能存在的数组格式或对象格式的IP数据
 */
public class IpInfoTypeHandler extends AbstractJsonTypeHandler<IpInfo> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected IpInfo parse(String json) {
        try {
            // 首先尝试按对象格式解析
            return OBJECT_MAPPER.readValue(json, IpInfo.class);
        } catch (Exception e) {
            // 如果解析失败，尝试按数组格式解析
            try {
                String[] ipArray = OBJECT_MAPPER.readValue(json, String[].class);
                if (ipArray != null && ipArray.length > 0) {
                    IpInfo ipInfo = new IpInfo();
                    ipInfo.setFirstIp(ipArray[0]);
                    ipInfo.setLatestIp(ipArray[0]);
                    // 设置默认地区信息
                    ipInfo.setFirstRegion("Unknown");
                    ipInfo.setLatestRegion("Unknown");
                    return ipInfo;
                }
            } catch (Exception arrayException) {
                // 如果两种格式都失败，则返回空的IpInfo对象
                IpInfo ipInfo = new IpInfo();
                ipInfo.setFirstRegion("Unknown");
                ipInfo.setLatestRegion("Unknown");
                return ipInfo;
            }
            
            // 如果两种格式都失败，则返回空的IpInfo对象
            IpInfo ipInfo = new IpInfo();
            ipInfo.setFirstRegion("Unknown");
            ipInfo.setLatestRegion("Unknown");
            return ipInfo;
        }
    }

    @Override
    protected String toJson(IpInfo obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}