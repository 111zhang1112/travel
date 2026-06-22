package com.tourism.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.entity.PaymentLog;
import com.tourism.mapper.PaymentLogMapper;
import org.springframework.stereotype.Service;

/**
 * 支付日志服务
 */
@Service
public class PaymentLogService extends ServiceImpl<PaymentLogMapper, PaymentLog> {
    
    /**
     * 记录日志
     */
    public void log(String orderNo, String tradeNo, String logType, String requestData, String responseData) {
        PaymentLog log = new PaymentLog();
        log.setOrderNo(orderNo);
        log.setTradeNo(tradeNo);
        log.setLogType(logType);
        log.setRequestData(requestData);
        log.setResponseData(responseData);
        save(log);
    }
}
