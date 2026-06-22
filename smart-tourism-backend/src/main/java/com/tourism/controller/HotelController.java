package com.tourism.controller;

import com.tourism.common.PageResult;
import com.tourism.common.Result;
import com.tourism.entity.Hotel;
import com.tourism.entity.HotelRoom;
import com.tourism.service.HotelService;
import com.tourism.service.HotelRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 酒店控制器（公开接口）
 */
@RestController
@RequestMapping("/api/hotel")
public class HotelController {

    @Autowired
    private HotelService hotelService;
    
    @Autowired
    @Lazy
    private HotelRoomService hotelRoomService;

    /**
     * 分页查询酒店列表
     */
    @GetMapping("/list")
    public Result<PageResult<Hotel>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) Integer starRating,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String sortBy
    ) {
        PageResult<Hotel> result = hotelService.getPublicList(page, size, keyword, priceMin, priceMax, starRating, region, minRating, sortBy);
        return Result.success(result);
    }

    /**
     * 搜索酒店
     */
    @GetMapping("/search")
    public Result<List<Hotel>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) Integer starRating
    ) {
        List<Hotel> result = hotelService.search(keyword, priceMin, priceMax, starRating);
        return Result.success(result);
    }

    /**
     * 获取酒店详情
     */
    @GetMapping("/detail/{id}")
    public Result<Hotel> detail(@PathVariable Long id) {
        Hotel hotel = hotelService.getDetail(id);
        return Result.success(hotel);
    }

    /**
     * 获取酒店房间列表
     */
    @GetMapping("/{hotelId}/rooms")
    public Result<List<HotelRoom>> getRooms(@PathVariable Long hotelId) {
        List<HotelRoom> rooms = hotelRoomService.getPublicRoomsByHotelId(hotelId);
        return Result.success(rooms);
    }
}
