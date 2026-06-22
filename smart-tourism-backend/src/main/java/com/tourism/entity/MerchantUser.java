package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家用户实体
 */
@Data
@TableName("merchant_users")
public class MerchantUser {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;
    
    private String merchantName;
    
    private String merchantType;  // SCENIC-景点, HOTEL-酒店
    
    private String contactPerson;
    
    private String contactPhone;
    
    private String email;
    
    private String businessLicense;  // 营业执照图片URL
    
    private String address;
    
    private String description;
    
    private String status;  // PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝, DISABLED-已禁用
    
    private String rejectReason;  // 拒绝原因
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
