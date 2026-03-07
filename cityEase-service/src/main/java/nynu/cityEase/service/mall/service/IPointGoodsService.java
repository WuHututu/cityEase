package nynu.cityEase.service.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import nynu.cityEase.api.vo.mall.PointGoodsPageReq;
import nynu.cityEase.api.vo.mall.PointGoodsSaveReq;
import nynu.cityEase.api.vo.mall.PointGoodsVO;

public interface IPointGoodsService {

    IPage<PointGoodsVO> page(PointGoodsPageReq req);

    PointGoodsVO detail(Long id);

    void saveOrUpdate(PointGoodsSaveReq req);

    void removeById(Long id);

    void changeStatus(Long id, Integer status);
}
