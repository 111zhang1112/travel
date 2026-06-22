package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 酒店房间实体
 */
@Data
@TableName("hotel_room")
public class HotelRoom {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long hotelId;
    
    private String name;
    
    private String description;
    
    private String images;  // JSON 数组
    
    private BigDecimal price;
    
    private String bedType;  // 床型
    
    private Integer area;  // 面积
    
    private Integer maxGuests;  // 最大入住人数
    
    private Integer breakfast;  // 是否含早餐
    
    private Integer wifi;  // 是否有WiFi
    
    @TableField("`window`")
    private Integer window;  // 是否有窗
    
    private Integer totalRooms;  // 总房间数
    
    private Integer availableRooms;  // 可用房间数
    
    private Integer status;  // 0-下架 1-上架
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
