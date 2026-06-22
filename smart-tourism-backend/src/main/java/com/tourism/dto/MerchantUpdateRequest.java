package com.tourism.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家信息更新请求
 */
@Data
public class MerchantUpdateRequest {
    
    @Size(max = 50, message = "联系人姓名不能超过50个字符")
    private String contactPerson;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;
    
    private String email;
    
    private String address;
    
    private String description;
    
    private String businessLicense;
}
