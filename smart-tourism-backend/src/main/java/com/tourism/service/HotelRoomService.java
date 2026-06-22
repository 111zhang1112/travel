package com.tourism.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.common.BusinessException;
import com.tourism.common.PageResult;
import com.tourism.entity.Hotel;
import com.tourism.entity.HotelRoom;
import com.tourism.mapper.HotelRoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 酒店房间服务
 */
@Service
public class HotelRoomService extends ServiceImpl<HotelRoomMapper, HotelRoom> {

    @Autowired
    @Lazy
    private HotelService hotelService;

    /**
     * 获取酒店的房间列表（前台）
     */
    public List<HotelRoom> getPublicRoomsByHotelId(Long hotelId) {
        LambdaQueryWrapper<HotelRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HotelRoom::getHotelId, hotelId)
               .eq(HotelRoom::getStatus, 1)
               .orderByAsc(HotelRoom::getPrice);
        return list(wrapper);
    }

    /**
     * 获取房间详情
     */
    public HotelRoom getDetail(Long id) {
        HotelRoom room = getById(id);
        if (room == null || room.getStatus() != 1) {
            throw new BusinessException(404, "房间不存在");
        }
        return room;
    }

    // ========== 管理端方法 ==========

    /**
     * 管理端分页查询酒店房间
     */
    public PageResult<HotelRoom> getAdminList(Long hotelId, Integer page, Integer size) {
        Page<HotelRoom> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<HotelRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HotelRoom::getHotelId, hotelId)
               .orderByAsc(HotelRoom::getPrice);
        Page<HotelRoom> result = page(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 获取酒店所有房间（不分页）
     */
    public List<HotelRoom> getAdminRoomsByHotelId(Long hotelId) {
        LambdaQueryWrapper<HotelRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HotelRoom::getHotelId, hotelId)
               .orderByAsc(HotelRoom::getPrice);
        return list(wrapper);
    }

    /**
     * 创建房间
     */
    @Transactional
    public HotelRoom create(HotelRoom room) {
        // 验证酒店是否存在
        Hotel hotel = hotelService.getById(room.getHotelId());
        if (hotel == null) {
            throw new BusinessException(404, "酒店不存在");
        }
        
        validateRoom(room);
        room.setStatus(1);
        if (room.getAvailableRooms() == null) {
            room.setAvailableRooms(room.getTotalRooms());
        }
        save(room);
        
        // 更新酒店价格区间
        updateHotelPriceRange(room.getHotelId());
        
        return room;
    }

    /**
     * 更新房间
     */
    @Transactional
    public HotelRoom update(Long id, HotelRoom room) {
        HotelRoom existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "房间不存在");
        }

        validateRoom(room);
        room.setId(id);
        room.setHotelId(existing.getHotelId());  // 不允许修改所属酒店
        updateById(room);
        
        // 更新酒店价格区间
        updateHotelPriceRange(existing.getHotelId());
        
        return getById(id);
    }

    /**
     * 删除房间
     */
    @Transactional
    public void delete(Long id) {
        HotelRoom room = getById(id);
        if (room == null) {
            throw new BusinessException(404, "房间不存在");
        }
        
        Long hotelId = room.getHotelId();
        removeById(id);
        
        // 更新酒店价格区间
        updateHotelPriceRange(hotelId);
    }

    /**
     * 更新房间状态
     */
    @Transactional
    public void updateStatus(Long id, Integer status) {
        HotelRoom room = getById(id);
        if (room == null) {
            throw new BusinessException(404, "房间不存在");
        }
        room.setStatus(status);
        updateById(room);
        
        // 更新酒店价格区间
        updateHotelPriceRange(room.getHotelId());
    }

    /**
     * 更新酒店价格区间（根据房间价格）
     */
    private void updateHotelPriceRange(Long hotelId) {
        LambdaQueryWrapper<HotelRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HotelRoom::getHotelId, hotelId)
               .eq(HotelRoom::getStatus, 1);
        List<HotelRoom> rooms = list(wrapper);
        
        if (rooms.isEmpty()) {
            return;
        }
        
        BigDecimal minPrice = rooms.stream()
                .map(HotelRoom::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal maxPrice = rooms.stream()
                .map(HotelRoom::getPrice)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        Hotel hotel = hotelService.getById(hotelId);
        if (hotel != null) {
            hotel.setPriceMin(minPrice);
            hotel.setPriceMax(maxPrice);
            hotelService.updateById(hotel);
        }
    }

    private void validateRoom(HotelRoom room) {
        if (!StringUtils.hasText(room.getName())) {
            throw new BusinessException(400, "房型名称不能为空");
        }
        if (room.getPrice() == null || room.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "房间价格必须大于0");
        }
        if (room.getTotalRooms() == null || room.getTotalRooms() <= 0) {
            throw new BusinessException(400, "房间数量必须大于0");
        }
    }

    // ========== 商家端方法 ==========

    /**
     * 商家获取自己酒店的房型列表（验证权限）
     */
    public List<HotelRoom> getMerchantRoomsByHotelId(Long merchantId, Long hotelId) {
        // 验证酒店归属
        Hotel hotel = hotelService.getById(hotelId);
        if (hotel == null) {
            throw new BusinessException(404, "酒店不存在");
        }
        if (!merchantId.equals(hotel.getMerchantId())) {
            throw new BusinessException(403, "无权查看其他商家的酒店房型");
        }
        
        LambdaQueryWrapper<HotelRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HotelRoom::getHotelId, hotelId)
               .orderByAsc(HotelRoom::getPrice);
        return list(wrapper);
    }

    /**
     * 商家创建房型（验证权限）
     */
    @Transactional
    public HotelRoom createByMerchant(Long merchantId, HotelRoom room) {
        // 验证酒店归属
        Hotel hotel = hotelService.getById(room.getHotelId());
        if (hotel == null) {
            throw new BusinessException(404, "酒店不存在");
        }
        if (!merchantId.equals(hotel.getMerchantId())) {
            throw new BusinessException(403, "无权为其他商家的酒店添加房型");
        }
        
        validateRoom(room);
        room.setStatus(1);
        if (room.getAvailableRooms() == null) {
            room.setAvailableRooms(room.getTotalRooms());
        }
        save(room);
        
        // 更新酒店价格区间
        updateHotelPriceRange(room.getHotelId());
        
        return room;
    }

    /**
     * 商家更新房型（验证权限）
     */
    @Transactional
    public HotelRoom updateByMerchant(Long merchantId, Long id, HotelRoom room) {
        HotelRoom existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "房型不存在");
        }
        
        // 验证酒店归属
        Hotel hotel = hotelService.getById(existing.getHotelId());
        if (hotel == null) {
            throw new BusinessException(404, "酒店不存在");
        }
        if (!merchantId.equals(hotel.getMerchantId())) {
            throw new BusinessException(403, "无权修改其他商家的房型");
        }
        
        validateRoom(room);
        room.setId(id);
        room.setHotelId(existing.getHotelId());  // 不允许修改所属酒店
        updateById(room);
        
        // 更新酒店价格区间
        updateHotelPriceRange(existing.getHotelId());
        
        return getById(id);
    }

    /**
     * 商家删除房型（验证权限）
     */
    @Transactional
    public void deleteByMerchant(Long merchantId, Long id) {
        HotelRoom room = getById(id);
        if (room == null) {
            throw new BusinessException(404, "房型不存在");
        }
        
        // 验证酒店归属
        Hotel hotel = hotelService.getById(room.getHotelId());
        if (hotel == null) {
            throw new BusinessException(404, "酒店不存在");
        }
        if (!merchantId.equals(hotel.getMerchantId())) {
            throw new BusinessException(403, "无权删除其他商家的房型");
        }
        
        Long hotelId = room.getHotelId();
        removeById(id);
        
        // 更新酒店价格区间
        updateHotelPriceRange(hotelId);
    }

    /**
     * 商家更新房型状态（验证权限）
     */
    @Transactional
    public void updateStatusByMerchant(Long merchantId, Long id, Integer status) {
        HotelRoom room = getById(id);
        if (room == null) {
            throw new BusinessException(404, "房型不存在");
        }
        
        // 验证酒店归属
        Hotel hotel = hotelService.getById(room.getHotelId());
        if (hotel == null) {
            throw new BusinessException(404, "酒店不存在");
        }
        if (!merchantId.equals(hotel.getMerchantId())) {
            throw new BusinessException(403, "无权修改其他商家的房型");
        }
        
        if (status != 0 && status != 1) {
            throw new BusinessException(400, "状态值无效，只能为0或1");
        }
        
        room.setStatus(status);
        updateById(room);
        
        // 更新酒店价格区间
        updateHotelPriceRange(room.getHotelId());
    }
}
