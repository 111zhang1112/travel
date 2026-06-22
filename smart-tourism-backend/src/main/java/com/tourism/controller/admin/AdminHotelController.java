package com.tourism.controller.admin;

import com.tourism.common.PageResult;
import com.tourism.common.Result;
import com.tourism.entity.Hotel;
import com.tourism.entity.HotelRoom;
import com.tourism.service.HotelService;
import com.tourism.service.HotelRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 酒店管理控制器（管理端）
 */
@RestController
@RequestMapping("/api/admin/hotel")
public class AdminHotelController {

    @Autowired
    private HotelService hotelService;
    
    @Autowired
    @Lazy
    private HotelRoomService hotelRoomService;

    @GetMapping("/list")
    public Result<PageResult<Hotel>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        PageResult<Hotel> result = hotelService.getAdminList(page, size, keyword, status);
        return Result.success(result);
    }

    @PostMapping("/create")
    public Result<Hotel> create(@RequestBody Hotel hotel) {
        Hotel result = hotelService.create(hotel);
        return Result.success(result);
    }

    @PutMapping("/update/{id}")
    public Result<Hotel> update(@PathVariable Long id, @RequestBody Hotel hotel) {
        Hotel result = hotelService.update(id, hotel);
        return Result.success(result);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        hotelService.softDelete(id);
        return Result.success();
    }

    // ========== 房间管理接口 ==========

    @GetMapping("/{hotelId}/rooms")
    public Result<List<HotelRoom>> getRooms(@PathVariable Long hotelId) {
        List<HotelRoom> rooms = hotelRoomService.getAdminRoomsByHotelId(hotelId);
        return Result.success(rooms);
    }

    @PostMapping("/{hotelId}/rooms")
    public Result<HotelRoom> createRoom(@PathVariable Long hotelId, @RequestBody HotelRoom room) {
        room.setHotelId(hotelId);
        HotelRoom result = hotelRoomService.create(room);
        return Result.success(result);
    }

    @PutMapping("/rooms/{roomId}")
    public Result<HotelRoom> updateRoom(@PathVariable Long roomId, @RequestBody HotelRoom room) {
        HotelRoom result = hotelRoomService.update(roomId, room);
        return Result.success(result);
    }

    @DeleteMapping("/rooms/{roomId}")
    public Result<Void> deleteRoom(@PathVariable Long roomId) {
        hotelRoomService.delete(roomId);
        return Result.success();
    }

    @PutMapping("/rooms/{roomId}/status")
    public Result<Void> updateRoomStatus(@PathVariable Long roomId, @RequestParam Integer status) {
        hotelRoomService.updateStatus(roomId, status);
        return Result.success();
    }
}
