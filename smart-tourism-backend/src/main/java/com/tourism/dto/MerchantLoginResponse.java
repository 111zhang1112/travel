package com.tourism.dto;

import lombok.Data;

/**
 * 商家登录响应
 */
@Data
public class MerchantLoginResponse {
    
    private String token;
    
    private MerchantInfoResponse merchantInfo;
}
