package com.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家注册请求
 */
@Data
public class MerchantRegisterRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度为3-50个字符")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20个字符")
    private String password;
    
    @NotBlank(message = "商家名称不能为空")
    @Size(max = 100, message = "商家名称不能超过100个字符")
    private String merchantName;
    
    @NotBlank(message = "商家类型不能为空")
    @Pattern(regexp = "SCENIC|HOTEL", message = "商家类型必须是SCENIC或HOTEL")
    private String merchantType;
    
    @NotBlank(message = "联系人不能为空")
    @Size(max = 50, message = "联系人姓名不能超过50个字符")
    private String contactPerson;
    
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;
    
    private String email;
    
    private String businessLicense;
    
    private String address;
    
    private String description;
}
