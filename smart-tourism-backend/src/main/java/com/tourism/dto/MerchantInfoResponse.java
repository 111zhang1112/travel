package com.tourism.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家信息响应
 */
@Data
public class MerchantInfoResponse {
    
    private Long id;
    
    private String username;
    
    private String merchantName;
    
    private String merchantType;
    
    private String contactPerson;
    
    private String contactPhone;
    
    private String email;
    
    private String businessLicense;
    
    private String address;
    
    private String description;
    
    private String status;
    
    private String rejectReason;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
