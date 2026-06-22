package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付日志实体
 */
@Data
@TableName("payment_log")
public class PaymentLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private String tradeNo;
    private String logType;
    private String requestData;
    private String responseData;
    private LocalDateTime createTime;
}
