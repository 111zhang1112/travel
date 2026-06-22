package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问答问题实体
 */
@Data
@TableName("qa_question")
public class QaQuestion {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String title;
    
    private String content;
    
    private String tags;
    
    /**
     * 分类：SCENIC-景点 HOTEL-酒店 ROUTE-路线 FOOD-美食 TRAFFIC-交通 OTHER-其他
     */
    private String category;
    
    private Integer viewCount;
    
    private Integer answerCount;
    
    private Integer likeCount;
    
    /**
     * 是否已解决：0-未解决 1-已解决
     */
    private Integer isSolved;
    
    /**
     * 最佳答案ID
     */
    private Long bestAnswerId;
    
    /**
     * 状态：0-待审核 1-已发布 2-已拒绝 3-已删除
     */
    private Integer status;
    
    private String rejectReason;
    
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
