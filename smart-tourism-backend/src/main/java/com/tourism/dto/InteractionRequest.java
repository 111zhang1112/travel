package com.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户行为记录请求
 */
@Data
public class InteractionRequest {
    
    @NotBlank(message = "目标类型不能为空")
    private String targetType;  // SCENIC, HOTEL, GUIDE
    
    @NotNull(message = "目标ID不能为空")
    private Long targetId;
    
    @NotBlank(message = "行为类型不能为空")
    private String actionType;  // VIEW, FAVORITE, LIKE
}
