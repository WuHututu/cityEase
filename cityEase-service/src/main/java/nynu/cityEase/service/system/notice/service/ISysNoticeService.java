package nynu.cityEase.service.system.notice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import nynu.cityEase.api.vo.system.AdminNoticeQueryReq;
import nynu.cityEase.api.vo.system.AppNoticeQueryReq;
import nynu.cityEase.api.vo.system.NoticeDetailVO;
import nynu.cityEase.api.vo.system.NoticeListVO;
import nynu.cityEase.api.vo.system.NoticeSaveReq;
import nynu.cityEase.service.system.notice.repository.entity.SysNoticeDO;

/**
 * 公告服务
 */
public interface ISysNoticeService extends IService<SysNoticeDO> {

    /**
     * 后台：新增或修改公告
     */
    void saveOrUpdateNotice(NoticeSaveReq req);

    /**
     * App端：分页查询已发布的公告列表 (不含正文)
     */
    Page<NoticeListVO> getPublishedNotices(AppNoticeQueryReq req);

    /**
     * App 端：获取公告完整详情
     */
    NoticeDetailVO getNoticeDetail(Long id);

    /**
     * 管理端：分页查询公告列表（支持类型、状态筛选）
     */
    Page<NoticeListVO> page(AdminNoticeQueryReq req);

    /**
     * 管理端：删除公告
     */
    void deleteNotice(Long id);

    /**
     * 管理端：切换置顶状态
     */
    void toggleTop(Long id);
}
