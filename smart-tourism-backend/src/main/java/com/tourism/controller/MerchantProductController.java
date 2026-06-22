package com.tourism.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.tourism.common.PageResult;
import com.tourism.common.Result;
import com.tourism.entity.Hotel;
import com.tourism.entity.HotelRoom;
import com.tourism.entity.MerchantUser;
import com.tourism.entity.ScenicSpot;
import com.tourism.service.HotelRoomService;
import com.tourism.service.HotelService;
import com.tourism.service.MerchantUserService;
import com.tourism.service.ScenicSpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商家产品管理控制器
 */
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantProductController {

    private final ScenicSpotService scenicSpotService;
    private final HotelService hotelService;
    private final HotelRoomService hotelRoomService;
    private final MerchantUserService merchantUserService;

    /**
     * 获取当前商家ID和类型
     */
    private Long getCurrentMerchantId() {
        return StpUtil.getLoginIdAsLong();
    }

    private MerchantUser getCurrentMerchant() {
        Long merchantId = getCurrentMerchantId();
        return merchantUserService.getById(merchantId);
    }

    // ========== 景点管理接口 ==========

    /**
     * 获取商家的景点列表
     */
    @GetMapping("/scenic/list")
    public Result<PageResult<ScenicSpot>> getMerchantScenicList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        
        MerchantUser merchant = getCurrentMerchant();
        if (!"SCENIC".equals(merchant.getMerchantType())) {
            return Result.error("只有景点商家才能管理景点");
        }
        
        PageResult<ScenicSpot> result = scenicSpotService.getMerchantList(
                getCurrentMerchantId(), page, size, keyword, status);
        return Result.success(result);
    }

    /**
     * 获取景点详情
     */
    @GetMapping("/scenic/{id}")
    public Result<ScenicSpot> getMerchantScenicDetail(@PathVariable Long id) {
        MerchantUser merchant = getCurrentMerchant();
        if (!"SCENIC".equals(merchant.getMerchantType())) {
            return Result.error("只有景点商家才能查看景点");
        }
        
        ScenicSpot spot = scenicSpotService.getMerchantDetail(getCurrentMerchantId(), id);
        return Result.success(spot);
    }

    /**
     * 创建景点
     */
    @PostMapping("/scenic")
    public Result<ScenicSpot> createScenic(@RequestBody ScenicSpot spot) {
        MerchantUser merchant = getCurrentMerchant();
        if (!"SCENIC".equals(merchant.getMerchantType())) {
            return Result.error("只有景点商家才能创建景点");
        }
        
        ScenicSpot created = scenicSpotService.createByMerchant(getCurrentMerchantId(), spot);
        return Result.success(created);
    }

    /**
     * 更新景点
     */
    @PutMapping("/scenic/{id}")
    public Result<ScenicSpot> updateScenic(@PathVariable Long id, @RequestBody ScenicSpot spot) {
        MerchantUser merchant = getCurrentMerchant();
        if (!"SCENIC".equals(merchant.getMerchantType())) {
            return Result.error("只有景点商家才能更新景点");
        }
        
        ScenicSpot updated = scenicSpotService.updateByMerchant(getCurrentMerchantId(), id, spot);
        return Result.success(updated);
    }

    /**
     * 删除景点
     */
    @DeleteMapping("/scenic/{id}")
    public Result<Void> deleteScenic(@PathVariable Long id) {
        MerchantUser merchant = getCurrentMerchant();
        if (!"SCENIC".equals(merchant.getMerchantType())) {
            return Result.error("只有景点商家才能删除景点");
        }
        
        scenicSpotService.deleteByMerchant(getCurrentMerchantId(), id);
        return Result.success();
    }

    /**
     * 更新景点状态
     */
    @PutMapping("/scenic/{id}/status")
    public Result<Void> updateScenicStatus(@PathVariable Long id, @RequestParam Integer status) {
        MerchantUser merchant = getCurrentMerchant();
        if (!"SCENIC".equals(merchant.getMerchantType())) {
            return Result.error("只有景点商家才能修改景点状态");
        }
        
        scenicSpotService.updateStatusByMerchant(getCurrentMerchantId(), id, status);
        return Result.success();
    }

    // ========== 酒店管理接口 ==========

    /**
     * 获取商家的酒店列表
     */
    @GetMapping("/hotel/list")
    public Result<PageResult<Hotel>> getMerchantHotelList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        
        MerchantUser merchant = getCurrentMerchant();
        if (!"HOTEL".equals(merchant.getMerchantType())) {
            return Result.error("只有酒店商家才能管理酒店");
        }
        
        PageResult<Hotel> result = hotelService.getMerchantList(
                getCurrentMerchantId(), page, size, keyword, status);
        return Result.success(result);
    }

    /**
     * 获取酒店详情
     */
    @GetMapping("/hotel/{id}")
    public Result<Hotel> getMerchantHotelDetail(@PathVariable Long id) {
        MerchantUser merchant = getCurrentMerchant();
        if (!"HOTEL".equals(merchant.getMerchantType())) {
            return Result.error("只有酒店商家才能查看酒店");
        }
        
        Hotel hotel = hotelService.getMerchantDetail(getCurrentMerchantId(), id);
        return Result.success(hotel);
    }

    /**
     * 创建酒店
     */
    @PostMapping("/hotel")
    public Result<Hotel> createHotel(@RequestBody Hotel hotel) {
        MerchantUser merchant = getCurrentMerchant();
        if (!"HOTEL".equals(merchant.getMerchantType())) {
            return Result.error("只有酒店商家才能创建酒店");
        }
        
        Hotel created = hotelService.createByMerchant(getCurrentMerchantId(), hotel);
        return Result.success(created);
    }

    /**
     * 更新酒店
     */
    @PutMapping("/hotel/{id}")
    public Result<Hotel> updateHotel(@PathVariable Long id, @RequestBody Hotel hotel) {
        MerchantUser merchant = getCurrentMerchant();
        if (!"HOTEL".equals(merchant.getMerchantType())) {
            return Result.error("只有酒店商家才能更新酒店");
        }
        
        Hotel updated = hotelService.updateByMerchant(getCurrentMerchantId(), id, hotel);
        return Result.success(updated);
    }

    /**
     * 删除酒店
     */
    @DeleteMapping("/hotel/{id}")
    public Result<Void> deleteHotel(@PathVariable Long id) {
        MerchantUser merchant = getCurrentMerchant();
        if (!"HOTEL".equals(merchant.getMerchantType())) {
            return Result.error("只有酒店商家才能删除酒店");
        }
        
        hotelService.deleteByMerchant(getCurrentMerchantId(), id);
        return Result.success();
    }

    /**
     * 更新酒店状态
     */
    @PutMapping("/hotel/{id}/status")
    public Result<Void> updateHotelStatus(@PathVariable Long id, @RequestParam Integer status) {
        MerchantUser merchant = getCurrentMerchant();
        if (!"HOTEL".equals(merchant.getMerchantType())) {
            return Result.error("只有酒店商家才能修改酒店状态");
        }
        
        hotelService.updateStatusByMerchant(getCurrentMerchantId(), id, status);
        return Result.success();
    }

    // ========== 房型管理接口 ==========

    /**
     * 获取酒店的房型列表
     */
    @GetMapping("/hotel/{hotelId}/rooms")
    public Result<List<HotelRoom>> getMerchantRoomList(@PathVariable Long hotelId) {
        MerchantUser merchant = getCurrentMerchant();
        if (!"HOTEL".equals(merchant.getMerchantType())) {
            return Result.error("只有酒店商家才能管理房型");
        }
        
        List<HotelRoom> rooms = hotelRoomService.getMerchantRoomsByHotelId(
                getCurrentMerchantId(), hotelId);
        return Result.success(rooms);
    }

    /**
     * 创建房型
     */
    @PostMapping("/hotel/{hotelId}/rooms")
    public Result<HotelRoom> createRoom(@PathVariable Long hotelId, @RequestBody HotelRoom room) {
        MerchantUser merchant = getCurrentMerchant();
        if (!"HOTEL".equals(merchant.getMerchantType())) {
            return Result.error("只有酒店商家才能创建房型");
        }
        
        room.setHotelId(hotelId);
        HotelRoom created = hotelRoomService.createByMerchant(getCurrentMerchantId(), room);
        return Result.success(created);
    }

    /**
     * 更新房型
     */
    @PutMapping("/hotel/rooms/{id}")
    public Result<HotelRoom> updateRoom(@PathVariable Long id, @RequestBody HotelRoom room) {
        MerchantUser merchant = getCurrentMerchant();
        if (!"HOTEL".equals(merchant.getMerchantType())) {
            return Result.error("只有酒店商家才能更新房型");
        }
        
        HotelRoom updated = hotelRoomService.updateByMerchant(getCurrentMerchantId(), id, room);
        return Result.success(updated);
    }

    /**
     * 删除房型
     */
    @DeleteMapping("/hotel/rooms/{id}")
    public Result<Void> deleteRoom(@PathVariable Long id) {
        MerchantUser merchant = getCurrentMerchant();
        if (!"HOTEL".equals(merchant.getMerchantType())) {
            return Result.error("只有酒店商家才能删除房型");
        }
        
        hotelRoomService.deleteByMerchant(getCurrentMerchantId(), id);
        return Result.success();
    }

    /**
     * 更新房型状态
     */
    @PutMapping("/hotel/rooms/{id}/status")
    public Result<Void> updateRoomStatus(@PathVariable Long id, @RequestParam Integer status) {
        MerchantUser merchant = getCurrentMerchant();
        if (!"HOTEL".equals(merchant.getMerchantType())) {
            return Result.error("只有酒店商家才能修改房型状态");
        }
        
        hotelRoomService.updateStatusByMerchant(getCurrentMerchantId(), id, status);
        return Result.success();
    }
}
