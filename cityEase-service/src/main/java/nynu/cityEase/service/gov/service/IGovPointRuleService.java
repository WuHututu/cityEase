package nynu.cityEase.service.gov.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import nynu.cityEase.api.vo.gov.PointRuleLogVO;
import nynu.cityEase.api.vo.gov.PointRuleSaveReq;
import nynu.cityEase.api.vo.gov.PointRuleVO;

import java.util.List;

/**
 * 积分规则管理服务接口
 * Created: 2026/3/18
 */
public interface IGovPointRuleService {

    /**
     * 分页查询积分规则列表
     * @param current 当前页码
     * @param size 每页大小
     * @param ruleType 规则类型，可选
     * @param status 状态，可选
     * @return 规则分页列表
     */
    IPage<PointRuleVO> pageRules(Integer current, Integer size, Integer ruleType, Integer status);

    /**
     * 获取规则详情
     * @param id 规则ID
     * @return 规则详情
     */
    PointRuleVO getRuleDetail(Long id);

    /**
     * 保存或更新规则
     * @param req 规则保存请求
     */
    void saveOrUpdateRule(PointRuleSaveReq req);

    /**
     * 删除规则
     * @param id 规则ID
     */
    void deleteRule(Long id);

    /**
     * 启用/禁用规则
     * @param id 规则ID
     * @param status 状态：1-启用，0-禁用
     */
    void changeRuleStatus(Long id, Integer status);

    /**
     * 获取所有启用的规则
     * @return 启用的规则列表
     */
    List<PointRuleVO> getEnabledRules();

    /**
     * 分页查询规则执行记录
     * @param current 当前页码
     * @param size 每页大小
     * @param ruleId 规则ID，可选
     * @param roomId 房屋ID，可选
     * @param executionResult 执行结果，可选
     * @return 执行记录分页列表
     */
    IPage<PointRuleLogVO> pageRuleLogs(Integer current, Integer size, Long ruleId, Long roomId, Integer executionResult);

    /**
     * 手动触发规则执行
     * @param ruleId 规则ID
     * @param roomId 房屋ID
     * @param userId 用户ID
     * @param triggerEvent 触发事件
     * @param triggerData 触发数据JSON
     * @return 执行结果
     */
    boolean executeRule(Long ruleId, Long roomId, Long userId, String triggerEvent, String triggerData);
}
