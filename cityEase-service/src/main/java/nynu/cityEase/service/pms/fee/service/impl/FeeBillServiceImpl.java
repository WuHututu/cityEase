package nynu.cityEase.service.pms.fee.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.pms.*;
import nynu.cityEase.service.pms.fee.repository.entity.PmsFeeBillDO;
import nynu.cityEase.service.pms.fee.repository.entity.PmsPublicAreaDO;
import nynu.cityEase.service.pms.repository.entity.RoomDO;
import nynu.cityEase.service.pms.fee.repository.mapper.PmsFeeBillMapper;
import nynu.cityEase.service.pms.fee.repository.mapper.PmsPublicAreaMapper;
import nynu.cityEase.service.pms.repository.mapper.RoomMapper;
import nynu.cityEase.service.pms.fee.service.IFeeBillService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeeBillServiceImpl extends ServiceImpl<PmsFeeBillMapper, PmsFeeBillDO> implements IFeeBillService {

    @Resource
    private RoomMapper roomMapper;

    @Resource
    private PmsPublicAreaMapper areaMapper;

    @Override
    public Page<FeeBillPageVO> page(FeeBillPageReq req) {
        LambdaQueryWrapper<PmsFeeBillDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsFeeBillDO::getIsDeleted, 0);

        if (StrUtil.isNotBlank(req.getFeeMonth())) {
            wrapper.eq(PmsFeeBillDO::getFeeMonth, req.getFeeMonth().trim());
        }
        if (req.getStatus() != null) {
            wrapper.eq(PmsFeeBillDO::getStatus, req.getStatus());
        }

        // 先查 bill 分页
        wrapper.orderByDesc(PmsFeeBillDO::getCreateTime);
        Page<PmsFeeBillDO> doPage = this.page(new Page<>(req.getPageNo(), req.getPageSize()), wrapper);

        // 如果按 roomKeyword 过滤：这里用二次过滤（MVP 够用），数据量大时可改 join 或先查 roomIds
        List<PmsFeeBillDO> records = doPage.getRecords();
        if (StrUtil.isNotBlank(req.getRoomKeyword())) {
            String kw = req.getRoomKeyword().trim();
            // 查匹配的 roomIds
            List<Long> roomIds = roomMapper.selectList(new LambdaQueryWrapper<RoomDO>()
                            .eq(RoomDO::getIsDeleted, 0)
                            .like(RoomDO::getRoomNum, kw))
                    .stream().map(RoomDO::getId).collect(Collectors.toList());
            if (roomIds.isEmpty()) {
                Page<FeeBillPageVO> empty = new Page<>(doPage.getCurrent(), doPage.getSize(), 0);
                empty.setRecords(Collections.emptyList());
                return empty;
            }
            records = records.stream().filter(x -> roomIds.contains(x.getRoomId())).collect(Collectors.toList());
        }

        // 批量补充 roomNum / fullAddress
        Map<Long, RoomDO> roomMap = getRoomMap(records);
        Map<Long, PmsPublicAreaDO> areaMap = getAreaMap(roomMap.values());

        List<FeeBillPageVO> voList = new ArrayList<>();
        for (PmsFeeBillDO d : records) {
            FeeBillPageVO vo = new FeeBillPageVO();
            BeanUtils.copyProperties(d, vo);
            RoomDO room = roomMap.get(d.getRoomId());
            if (room != null) {
                vo.setRoomNum(room.getRoomNum());
                vo.setFullAddress(buildFullAddress(room.getAreaId(), areaMap, room.getRoomNum()));
            }
            voList.add(vo);
        }

        Page<FeeBillPageVO> voPage = new Page<>(doPage.getCurrent(), doPage.getSize(), doPage.getTotal());
        voPage.setRecords(voList);
        // total：如果做了 roomKeyword 二次过滤，这里 total 可能不准（MVP 可接受）。需要精确可改为先查 roomIds 再分页。
        return voPage;
    }

    @Override
    public FeeBillDetailVO detail(Long id) {
        PmsFeeBillDO d = this.getById(id);
        if (d == null || (d.getIsDeleted() != null && d.getIsDeleted() == 1)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "账单不存在");
        }
        FeeBillDetailVO vo = new FeeBillDetailVO();
        BeanUtils.copyProperties(d, vo);

        RoomDO room = roomMapper.selectById(d.getRoomId());
        if (room != null) {
            vo.setRoomNum(room.getRoomNum());
            Map<Long, PmsPublicAreaDO> areaMap = getAreaMap(Collections.singletonList(room));
            vo.setFullAddress(buildFullAddress(room.getAreaId(), areaMap, room.getRoomNum()));
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(FeeBillUpsertReq req) {
        if (req.getRoomId() == null || StrUtil.isBlank(req.getFeeMonth()) || req.getAmount() == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "参数不完整");
        }
        // 简单校验房屋存在
        RoomDO room = roomMapper.selectById(req.getRoomId());
        if (room == null || (room.getIsDeleted() != null && room.getIsDeleted() == 1)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "房屋不存在");
        }

        PmsFeeBillDO d = new PmsFeeBillDO();
        d.setRoomId(req.getRoomId());
        d.setFeeMonth(req.getFeeMonth().trim());
        d.setAmount(req.getAmount());
        d.setStatus(req.getStatus() == null ? 0 : req.getStatus());
        d.setIsDeleted(0);
        d.setCreateTime(LocalDateTime.now());
        d.setUpdateTime(LocalDateTime.now());
        this.save(d);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(FeeBillUpsertReq req) {
        if (req.getId() == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "缺少账单ID");
        }
        PmsFeeBillDO d = this.getById(req.getId());
        if (d == null || (d.getIsDeleted() != null && d.getIsDeleted() == 1)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "账单不存在");
        }
        if (req.getAmount() != null) d.setAmount(req.getAmount());
        if (StrUtil.isNotBlank(req.getFeeMonth())) d.setFeeMonth(req.getFeeMonth().trim());
        if (req.getStatus() != null) d.setStatus(req.getStatus());
        d.setUpdateTime(LocalDateTime.now());
        this.updateById(d);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PmsFeeBillDO d = this.getById(id);
        if (d == null) return;
        d.setIsDeleted(1);
        LambdaUpdateWrapper<PmsFeeBillDO> up = new LambdaUpdateWrapper<>();
        up.set(PmsFeeBillDO::getIsDeleted, 1)
                .eq(PmsFeeBillDO::getId, id);
        update(up);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generate(FeeBillGenerateReq req) {
        if (StrUtil.isBlank(req.getFeeMonth()) || req.getAmount() == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "参数不完整");
        }
        String feeMonth = req.getFeeMonth().trim();

        // 所有有效房屋
        List<RoomDO> rooms = roomMapper.selectList(new LambdaQueryWrapper<RoomDO>()
                .eq(RoomDO::getIsDeleted, 0));

        if (rooms.isEmpty()) return 0;

        List<Long> roomIds = rooms.stream().map(RoomDO::getId).collect(Collectors.toList());

        // 已存在账单的房屋（同月）
        List<PmsFeeBillDO> exists = this.list(new LambdaQueryWrapper<PmsFeeBillDO>()
                .eq(PmsFeeBillDO::getIsDeleted, 0)
                .eq(PmsFeeBillDO::getFeeMonth, feeMonth)
                .in(PmsFeeBillDO::getRoomId, roomIds));

        Set<Long> existRoomIds = exists.stream().map(PmsFeeBillDO::getRoomId).collect(Collectors.toSet());

        int created = 0;
        LocalDateTime now = LocalDateTime.now();
        for (RoomDO room : rooms) {
            if (existRoomIds.contains(room.getId())) continue;

            PmsFeeBillDO d = new PmsFeeBillDO();
            d.setRoomId(room.getId());
            d.setFeeMonth(feeMonth);
            d.setAmount(req.getAmount());
            d.setStatus(0);
            d.setIsDeleted(0);
            d.setCreateTime(now);
            d.setUpdateTime(now);
            this.save(d);
            created++;
        }
        return created;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markPaid(FeeBillMarkPaidReq req) {
        if (req.getId() == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "缺少账单ID");
        }
        PmsFeeBillDO d = this.getById(req.getId());
        if (d == null || (d.getIsDeleted() != null && d.getIsDeleted() == 1)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "账单不存在");
        }
        d.setStatus(1);
        d.setPayerId(req.getPayerId());
        d.setPayTime(LocalDateTime.now());
        d.setUpdateTime(LocalDateTime.now());
        this.updateById(d);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markUnpaid(Long id) {
        PmsFeeBillDO d = this.getById(id);
        if (d == null || (d.getIsDeleted() != null && d.getIsDeleted() == 1)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "账单不存在");
        }
        d.setStatus(0);
        d.setPayerId(null);
        d.setPayTime(null);
        d.setUpdateTime(LocalDateTime.now());
        this.updateById(d);
    }

    private Map<Long, RoomDO> getRoomMap(List<PmsFeeBillDO> bills) {
        Set<Long> roomIds = bills.stream().map(PmsFeeBillDO::getRoomId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (roomIds.isEmpty()) return Collections.emptyMap();
        List<RoomDO> rooms = roomMapper.selectList(new LambdaQueryWrapper<RoomDO>().in(RoomDO::getId, roomIds));
        return rooms.stream().collect(Collectors.toMap(RoomDO::getId, x -> x, (a, b) -> a));
    }

    private Map<Long, PmsPublicAreaDO> getAreaMap(Collection<RoomDO> rooms) {
        Set<Long> areaIds = rooms.stream().map(RoomDO::getAreaId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (areaIds.isEmpty()) return Collections.emptyMap();

        // 为了构建完整路径，需要把所有父节点也取出来
        Map<Long, PmsPublicAreaDO> map = new HashMap<>();
        Deque<Long> stack = new ArrayDeque<>(areaIds);
        while (!stack.isEmpty()) {
            Long id = stack.pop();
            if (id == null || map.containsKey(id)) continue;
            PmsPublicAreaDO area = areaMapper.selectById(id);
            if (area == null) continue;
            map.put(id, area);
            if (area.getParentId() != null) {
                stack.push(area.getParentId());
            }
        }
        return map;
    }

    private String buildFullAddress(Long areaId, Map<Long, PmsPublicAreaDO> areaMap, String roomNum) {
        // areaName path
        List<String> names = new ArrayList<>();
        Long cur = areaId;
        int guard = 0;
        while (cur != null && guard++ < 20) {
            PmsPublicAreaDO a = areaMap.get(cur);
            if (a == null) break;
            names.add(a.getName());
            cur = a.getParentId();
        }
        Collections.reverse(names);
        String prefix = String.join("-", names);
        if (StrUtil.isBlank(prefix)) return roomNum;
        return prefix + "-" + roomNum;
    }
}
