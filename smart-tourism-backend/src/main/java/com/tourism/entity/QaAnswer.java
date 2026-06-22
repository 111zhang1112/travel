package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问答答案实体
 */
@Data
@TableName("qa_answer")
public class QaAnswer {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long questionId;
    
    private Long userId;
    
    private String content;
    
    private String images;
    
    private Integer likeCount;
    
    /**
     * 是否最佳答案：0-否 1-是
     */
    private Integer isBest;
    
    /**
     * 状态：0-已删除 1-正常 2-待审核
     */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 非数据库字段
    @TableField(exist = false)
    private String username;
    
    @TableField(exist = false)
    private String nickname;
    
    @TableField(exist = false)
    private String avatar;
}
