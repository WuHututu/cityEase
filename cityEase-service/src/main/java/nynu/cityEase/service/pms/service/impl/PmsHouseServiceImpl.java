package nynu.cityEase.service.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import nynu.cityEase.api.vo.pms.HouseQueryReq;
import nynu.cityEase.api.vo.pms.HouseVO;
import nynu.cityEase.service.pms.repository.entity.PublicAreaDO;
import nynu.cityEase.service.pms.repository.entity.RoomDO;
import nynu.cityEase.service.pms.repository.mapper.PublicAreaMapper;
import nynu.cityEase.service.pms.repository.mapper.RoomMapper;
import nynu.cityEase.service.pms.service.IPmsHouseService;
import nynu.cityEase.service.pms.service.IPmsPublicAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PmsHouseServiceImpl extends ServiceImpl<PublicAreaMapper, PublicAreaDO> implements IPmsHouseService {

    @Resource
    private RoomMapper roomMapper;

    @Resource
    private PublicAreaMapper publicAreaMapper;

    @Autowired
    private IPmsPublicAreaService publicAreaService;

    @Override
    public Page<HouseVO> getHousePage(HouseQueryReq req) {
        // 构建查询条件
        LambdaQueryWrapper<PublicAreaDO> areaWrapper = new LambdaQueryWrapper<>();
        
        // 如果指定了区域类型，且为0（公共区域），则查询公共区域
        if (req.getAreaType() != null && req.getAreaType() == 0) {
            // 只查询公共区域
            if (req.getAreaName() != null && !req.getAreaName().trim().isEmpty()) {
                areaWrapper.like(PublicAreaDO::getName, req.getAreaName());
            }
            // 这里我们只考虑层级较高的公共区域（比如小区级别或楼栋级别）
            areaWrapper.ge(PublicAreaDO::getLevel, 1); // 假设层级1以上为公共区域
            
            // 查询公共区域数据
            Page<PublicAreaDO> areaPage = publicAreaMapper.selectPage(req.toPage(), areaWrapper);
            
            // 转换为HouseVO
            Page<HouseVO> resultPage = new Page<>();
            resultPage.setCurrent(areaPage.getCurrent());
            resultPage.setSize(areaPage.getSize());
            resultPage.setTotal(areaPage.getTotal());
            
            List<HouseVO> houseVOList = new ArrayList<>();
            for (PublicAreaDO area : areaPage.getRecords()) {
                HouseVO vo = new HouseVO();
                vo.setId(area.getId());
                vo.setAreaType(0); // 公共区域
                vo.setAreaName(area.getName());
                vo.setAreaAddress(publicAreaService.getFullAreaName(area.getId()));
                vo.setCreateTime(area.getCreateTime());
                houseVOList.add(vo);
            }
            
            resultPage.setRecords(houseVOList);
            return resultPage;
        } 
        // 如果指定了区域类型，且为1（房屋），则查询房屋
        else if (req.getAreaType() != null && req.getAreaType() == 1) {
            // 查询房屋数据
            LambdaQueryWrapper<RoomDO> roomWrapper = new LambdaQueryWrapper<>();
            if (req.getAreaName() != null && !req.getAreaName().trim().isEmpty()) {
                // 模糊匹配房屋号
                roomWrapper.like(RoomDO::getRoomNum, req.getAreaName());
            }
            
            Page<RoomDO> roomPage = roomMapper.selectPage(req.toPage(), roomWrapper);
            
            // 转换为HouseVO
            Page<HouseVO> resultPage = new Page<>();
            resultPage.setCurrent(roomPage.getCurrent());
            resultPage.setSize(roomPage.getSize());
            resultPage.setTotal(roomPage.getTotal());
            
            List<HouseVO> houseVOList = new ArrayList<>();
            for (RoomDO room : roomPage.getRecords()) {
                HouseVO vo = new HouseVO();
                vo.setId(room.getId());
                vo.setAreaType(1); // 房屋
                vo.setAreaName(room.getRoomNum());
                
                // 获取完整地址
                String fullAreaName = publicAreaService.getFullAreaName(room.getAreaId());
                vo.setAreaAddress(fullAreaName + "-" + room.getRoomNum());
                
                vo.setCreateTime(room.getCreateTime());
                houseVOList.add(vo);
            }
            
            resultPage.setRecords(houseVOList);
            return resultPage;
        } 
        // 如果没有指定区域类型，查询两者并合并
        else {
            // 查询公共区域
            if (req.getAreaName() != null && !req.getAreaName().trim().isEmpty()) {
                areaWrapper.like(PublicAreaDO::getName, req.getAreaName());
            }
            areaWrapper.ge(PublicAreaDO::getLevel, 1); // 假设层级1以上为公共区域
            List<PublicAreaDO> areaList = publicAreaMapper.selectList(areaWrapper);
            
            // 查询房屋
            LambdaQueryWrapper<RoomDO> roomWrapper = new LambdaQueryWrapper<>();
            if (req.getAreaName() != null && !req.getAreaName().trim().isEmpty()) {
                roomWrapper.like(RoomDO::getRoomNum, req.getAreaName());
            }
            List<RoomDO> roomList = roomMapper.selectList(roomWrapper);
            
            // 合并结果并进行分页（需要手动实现）
            List<HouseVO> allResults = new ArrayList<>();
            
            // 添加公共区域
            for (PublicAreaDO area : areaList) {
                HouseVO vo = new HouseVO();
                vo.setId(area.getId());
                vo.setAreaType(0); // 公共区域
                vo.setAreaName(area.getName());
                vo.setAreaAddress(publicAreaService.getFullAreaName(area.getId()));
                vo.setCreateTime(area.getCreateTime());
                allResults.add(vo);
            }
            
            // 添加房屋
            for (RoomDO room : roomList) {
                HouseVO vo = new HouseVO();
                vo.setId(room.getId());
                vo.setAreaType(1); // 房屋
                vo.setAreaName(room.getRoomNum());
                
                // 获取完整地址
                String fullAreaName = publicAreaService.getFullAreaName(room.getAreaId());
                vo.setAreaAddress(fullAreaName + "-" + room.getRoomNum());
                
                vo.setCreateTime(room.getCreateTime());
                allResults.add(vo);
            }
            
            // 按创建时间倒序排列
            allResults.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
            
            // 手动分页
            int startIndex = (req.getPageNo() - 1) * req.getPageSize();
            int endIndex = Math.min(startIndex + req.getPageSize(), allResults.size());
            
            List<HouseVO> pagedResults = new ArrayList<>();
            if (startIndex < allResults.size()) {
                pagedResults = allResults.subList(startIndex, endIndex);
            }
            
            // 构建结果页
            Page<HouseVO> resultPage = new Page<>();
            resultPage.setCurrent(req.getPageNo());
            resultPage.setSize(req.getPageSize());
            resultPage.setTotal((long) allResults.size());
            resultPage.setRecords(pagedResults);
            
            return resultPage;
        }
    }
}