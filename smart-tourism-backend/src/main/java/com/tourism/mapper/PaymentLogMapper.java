package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.entity.PaymentLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付日志Mapper
 */
@Mapper
public interface PaymentLogMapper extends BaseMapper<PaymentLog> {
}
