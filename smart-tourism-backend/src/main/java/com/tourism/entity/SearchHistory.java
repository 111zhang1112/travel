package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 搜索历史记录实体
 */
@Data
@TableName("search_history")
public class SearchHistory {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID，未登录用户为null
     */
    private Long userId;
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 搜索类型：scenic/hotel/guide/all
     */
    private String searchType;
    
    /**
     * 搜索时间
     */
    private LocalDateTime searchTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
