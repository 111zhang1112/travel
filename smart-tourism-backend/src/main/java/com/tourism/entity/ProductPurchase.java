package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品购买记录实体
 */
@Data
@TableName("product_purchase")
public class ProductPurchase {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long productId;
    
    private BigDecimal pricePaid;
    
    private Integer quantity;
    
    private LocalDateTime purchaseDate;
    
    // 非数据库字段，用于展示
    @TableField(exist = false)
    private String productName;
    
    @TableField(exist = false)
    private String productImage;
    
    @TableField(exist = false)
    private String username;
}
