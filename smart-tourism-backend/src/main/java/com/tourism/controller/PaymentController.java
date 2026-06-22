package com.tourism.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.tourism.common.Result;
import com.tourism.config.AlipayConfig;
import com.tourism.dto.CreatePaymentRequest;
import com.tourism.entity.PaymentOrder;
import com.tourism.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final AlipayConfig alipayConfig;

    /**
     * 创建支付订单
     */
    @PostMapping("/create")
    @SaCheckLogin
    public Result<PaymentOrder> createPayment(@RequestBody CreatePaymentRequest request) {
        PaymentOrder payment = paymentService.createPayment(request);
        return Result.success(payment);
    }

    /**
     * 支付宝支付（自动识别PC/手机）
     */
    @GetMapping("/alipay/pay/{orderNo}")
    @SaCheckLogin
    public Result<String> alipayPay(@PathVariable String orderNo) throws AlipayApiException {
        String formHtml = paymentService.alipayPay(orderNo);
        return Result.success(formHtml);
    }

    /**
     * 支付宝异步通知
     */
    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        try {
            // 获取支付宝POST过来的所有参数
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                String[] values = requestParams.get(name);
                String valueStr = String.join(",", values);
                params.put(name, valueStr);
            }
            
            // 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                params,
                alipayConfig.getPublicKey(),
                alipayConfig.getCharset(),
                alipayConfig.getSignType()
            );
            
            if (signVerified) {
                // 处理通知
                paymentService.alipayNotify(params);
                return "success";
            } else {
                log.error("支付宝异步通知签名验证失败");
                return "failure";
            }
        } catch (Exception e) {
            log.error("处理支付宝异步通知失败", e);
            return "failure";
        }
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/status/{orderNo}")
    @SaCheckLogin
    public Result<PaymentOrder> queryStatus(@PathVariable String orderNo) throws AlipayApiException {
        PaymentOrder payment = paymentService.queryPaymentStatus(orderNo);
        return Result.success(payment);
    }
}
