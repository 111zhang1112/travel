package com.tourism.service;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.common.BusinessException;
import com.tourism.config.AlipayConfig;
import com.tourism.dto.CreatePaymentRequest;
import com.tourism.entity.PaymentLog;
import com.tourism.entity.PaymentOrder;
import com.tourism.mapper.PaymentOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * 支付服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService extends ServiceImpl<PaymentOrderMapper, PaymentOrder> {

    private final AlipayClient alipayClient;
    private final AlipayConfig alipayConfig;
    private final PaymentLogService paymentLogService;
    private final OrderService orderService;
    private final PointsService pointsService;

    /**
     * 创建支付订单
     */
    @Transactional
    public PaymentOrder createPayment(CreatePaymentRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        log.info("[PaymentService] 开始创建支付订单 - userId: {}, productType: {}, productId: {}, productName: {}, amount: {}, quantity: {}", 
                userId, request.getProductType(), request.getProductId(), request.getProductName(), 
                request.getAmount(), request.getQuantity());
        
        // 1. 使用OrderService创建业务订单（包含库存检查和扣减逻辑）
        com.tourism.dto.CreateOrderRequest orderRequest = new com.tourism.dto.CreateOrderRequest();
        orderRequest.setProductType(request.getProductType());
        orderRequest.setProductId(request.getProductId());
        orderRequest.setProductName(request.getProductName());
        orderRequest.setAmount(request.getAmount());
        orderRequest.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);
        
        // 如果是酒店订单，设置酒店特定字段
        if ("HOTEL".equals(request.getProductType()) && request.getRoomId() != null) {
            log.info("[PaymentService] 酒店订单额外参数 - roomId: {}, checkInDate: {}, checkOutDate: {}, nights: {}", 
                    request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate(), request.getNights());
            orderRequest.setRoomId(request.getRoomId());
            orderRequest.setCheckInDate(request.getCheckInDate());
            orderRequest.setCheckOutDate(request.getCheckOutDate());
            orderRequest.setNights(request.getNights());
        }
        
        // 调用OrderService.createOrder，会自动检查库存并扣减
        log.info("[PaymentService] 调用OrderService创建业务订单");
        com.tourism.entity.Order order = orderService.createOrder(orderRequest);
        log.info("[PaymentService] 业务订单创建成功 - orderNo: {}", order.getOrderNo());
        
        // 2. 创建支付订单
        PaymentOrder payment = new PaymentOrder();
        payment.setOrderNo(order.getOrderNo());
        payment.setUserId(userId);
        payment.setProductType(request.getProductType());
        payment.setProductId(request.getProductId());
        payment.setProductName(request.getProductName());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus("PENDING");
        save(payment);
        
        log.info("[PaymentService] 支付订单创建成功 - orderNo: {}, paymentMethod: {}, status: {}", 
                payment.getOrderNo(), payment.getPaymentMethod(), payment.getStatus());
        
        // 记录日志
        paymentLogService.log(payment.getOrderNo(), null, "CREATE", 
            JSON.toJSONString(request), "订单创建成功");
        
        return payment;
    }

    /**
     * 支付宝支付（自动识别PC/手机）
     */
    public String alipayPay(String orderNo) throws AlipayApiException {
        log.info("[PaymentService] 开始支付宝支付流程 - orderNo: {}", orderNo);
        
        // 获取User-Agent判断设备类型
        String userAgent = getUserAgent();
        boolean isMobile = isMobileDevice(userAgent);
        
        log.info("[PaymentService] 设备检测 - orderNo: {}, 设备类型: {}, User-Agent: {}", 
            orderNo, isMobile ? "移动端" : "PC端", userAgent);
        
        if (isMobile) {
            log.info("[PaymentService] 使用手机端H5支付 - orderNo: {}", orderNo);
            return alipayWapPay(orderNo);  // 手机端H5支付
        } else {
            log.info("[PaymentService] 使用PC端网页支付 - orderNo: {}", orderNo);
            return alipayPagePay(orderNo);  // PC端网页支付
        }
    }
    
    /**
     * 支付宝PC网页支付
     */
    public String alipayPagePay(String orderNo) throws AlipayApiException {
        log.info("[PaymentService] 开始PC网页支付 - orderNo: {}", orderNo);
        
        PaymentOrder payment = getByOrderNo(orderNo);
        validatePaymentOwner(payment);
        
        log.info("[PaymentService] 支付订单信息 - orderNo: {}, productType: {}, productName: {}, amount: {}, status: {}", 
                payment.getOrderNo(), payment.getProductType(), payment.getProductName(), 
                payment.getAmount(), payment.getStatus());
        
        if (!"PENDING".equals(payment.getStatus())) {
            log.error("[PaymentService] 订单状态不允许支付 - orderNo: {}, status: {}", orderNo, payment.getStatus());
            throw new BusinessException(400, "订单状态不允许支付");
        }
        
        // 创建支付请求
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());
        
        log.info("[PaymentService] 支付宝配置 - notifyUrl: {}, returnUrl: {}", 
                alipayConfig.getNotifyUrl(), alipayConfig.getReturnUrl());
        
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(payment.getOrderNo());
        model.setTotalAmount(payment.getAmount().toString());
        model.setSubject(payment.getProductName());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        model.setTimeoutExpress("30m");
        
        log.info("[PaymentService] 支付宝请求参数 - outTradeNo: {}, totalAmount: {}, subject: {}, productCode: {}", 
                model.getOutTradeNo(), model.getTotalAmount(), model.getSubject(), model.getProductCode());
        
        request.setBizModel(model);
        
        // 记录请求日志
        paymentLogService.log(orderNo, null, "CREATE", 
            JSON.toJSONString(model), "发起支付宝PC网页支付");
        
        // 调用支付宝接口
        log.info("[PaymentService] 调用支付宝API - orderNo: {}", orderNo);
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
        
        log.info("[PaymentService] 支付宝API响应 - orderNo: {}, isSuccess: {}, code: {}, msg: {}, subCode: {}, subMsg: {}", 
                orderNo, response.isSuccess(), response.getCode(), response.getMsg(), 
                response.getSubCode(), response.getSubMsg());
        
        if (response.isSuccess()) {
            // 更新订单状态为支付中
            payment.setStatus("PAYING");
            updateById(payment);
            
            log.info("[PaymentService] 支付宝表单生成成功 - orderNo: {}, 表单长度: {}", orderNo, response.getBody().length());
            
            // 记录响应日志
            paymentLogService.log(orderNo, null, "CREATE", 
                null, "支付宝表单生成成功");
            
            return response.getBody();
        } else {
            log.error("[PaymentService] 支付宝表单生成失败 - orderNo: {}, code: {}, msg: {}, subCode: {}, subMsg: {}", 
                    orderNo, response.getCode(), response.getMsg(), response.getSubCode(), response.getSubMsg());
            
            paymentLogService.log(orderNo, null, "CREATE", 
                null, "支付宝表单生成失败: " + response.getSubMsg());
            throw new BusinessException(500, "支付宝支付失败: " + response.getSubMsg());
        }
    }

    /**
     * 支付宝手机网站支付（H5支付）
     */
    public String alipayWapPay(String orderNo) throws AlipayApiException {
        PaymentOrder payment = getByOrderNo(orderNo);
        validatePaymentOwner(payment);
        
        if (!"PENDING".equals(payment.getStatus())) {
            throw new BusinessException(400, "订单状态不允许支付");
        }
        
        // 创建手机网站支付请求
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());
        
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(payment.getOrderNo());
        model.setTotalAmount(payment.getAmount().toString());
        model.setSubject(payment.getProductName());
        model.setProductCode("QUICK_WAP_WAY");  // 手机网站支付产品码
        model.setTimeoutExpress("30m");
        
        request.setBizModel(model);
        
        // 记录请求日志
        paymentLogService.log(orderNo, null, "CREATE", 
            JSON.toJSONString(model), "发起支付宝H5支付");
        
        // 调用支付宝接口
        AlipayTradeWapPayResponse response = alipayClient.pageExecute(request);
        
        if (response.isSuccess()) {
            // 更新订单状态为支付中
            payment.setStatus("PAYING");
            updateById(payment);
            
            // 记录响应日志
            paymentLogService.log(orderNo, null, "CREATE", 
                null, "支付宝H5表单生成成功");
            
            return response.getBody();
        } else {
            paymentLogService.log(orderNo, null, "CREATE", 
                null, "支付宝H5表单生成失败: " + response.getSubMsg());
            throw new BusinessException(500, "支付宝H5支付失败: " + response.getSubMsg());
        }
    }
    
    /**
     * 支付宝异步通知处理
     */
    @Transactional
    public void alipayNotify(Map<String, String> params) {
        String orderNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        
        log.info("收到支付宝异步通知: orderNo={}, tradeNo={}, status={}", 
            orderNo, tradeNo, tradeStatus);
        
        // 记录通知日志
        paymentLogService.log(orderNo, tradeNo, "NOTIFY", 
            JSON.toJSONString(params), "收到支付宝异步通知");
        
        PaymentOrder payment = getByOrderNo(orderNo);
        
        // 交易成功
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            if ("SUCCESS".equals(payment.getStatus())) {
                log.warn("订单已支付成功,忽略重复通知: {}", orderNo);
                return;
            }
            
            payment.setTradeNo(tradeNo);
            payment.setStatus("SUCCESS");
            payment.setPayTime(LocalDateTime.now());
            payment.setNotifyTime(LocalDateTime.now());
            updateById(payment);
            
            // 更新原订单状态
            try {
                com.tourism.entity.Order order = orderService.getByOrderNo(orderNo);
                order.setStatus("PAID");
                order.setPayTime(LocalDateTime.now());
                orderService.updateById(order);
                
                // 奖励积分
                pointsService.rewardOrderPoints(payment.getUserId(), order.getId(), payment.getAmount());
            } catch (Exception e) {
                log.error("更新订单状态失败", e);
            }
            
            log.info("订单支付成功: {}", orderNo);
        }
        // 交易关闭
        else if ("TRADE_CLOSED".equals(tradeStatus)) {
            payment.setTradeNo(tradeNo);
            payment.setStatus("CLOSED");
            payment.setNotifyTime(LocalDateTime.now());
            updateById(payment);
            log.info("订单已关闭: {}", orderNo);
        }
    }

    /**
     * 查询支付状态
     */
    public PaymentOrder queryPaymentStatus(String orderNo) throws AlipayApiException {
        PaymentOrder payment = getByOrderNo(orderNo);
        validatePaymentOwner(payment);
        
        // 如果已经是成功或关闭状态,直接返回
        if ("SUCCESS".equals(payment.getStatus()) || "CLOSED".equals(payment.getStatus())) {
            return payment;
        }
        
        // 查询支付宝订单状态
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(orderNo);
        request.setBizModel(model);
        
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        
        // 记录查询日志
        paymentLogService.log(orderNo, payment.getTradeNo(), "QUERY", 
            JSON.toJSONString(model), JSON.toJSONString(response));
        
        if (response.isSuccess()) {
            String tradeStatus = response.getTradeStatus();
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                payment.setTradeNo(response.getTradeNo());
                payment.setStatus("SUCCESS");
                payment.setPayTime(LocalDateTime.now());
                updateById(payment);
                
                // 更新原订单状态
                try {
                    com.tourism.entity.Order order = orderService.getByOrderNo(orderNo);
                    if (!"PAID".equals(order.getStatus())) {
                        order.setStatus("PAID");
                        order.setPayTime(LocalDateTime.now());
                        orderService.updateById(order);
                        
                        // 奖励积分
                        pointsService.rewardOrderPoints(payment.getUserId(), order.getId(), payment.getAmount());
                    }
                } catch (Exception e) {
                    log.error("更新订单状态失败", e);
                }
            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                payment.setStatus("CLOSED");
                updateById(payment);
            }
        }
        
        return payment;
    }

    /**
     * 根据订单号查询
     */
    public PaymentOrder getByOrderNo(String orderNo) {
        LambdaQueryWrapper<PaymentOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentOrder::getOrderNo, orderNo);
        PaymentOrder payment = getOne(wrapper);
        if (payment == null) {
            throw new BusinessException(404, "支付订单不存在");
        }
        return payment;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return timestamp + uuid;
    }

    /**
     * 验证订单所有者
     */
    private void validatePaymentOwner(PaymentOrder payment) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (!payment.getUserId().equals(currentUserId)) {
            throw new BusinessException(403, "无权操作此订单");
        }
    }
    
    /**
     * 获取User-Agent
     */
    private String getUserAgent() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String userAgent = request.getHeader("User-Agent");
            return userAgent != null ? userAgent : "";
        }
        return "";
    }
    
    /**
     * 判断是否为移动设备
     */
    private boolean isMobileDevice(String userAgent) {
        if (userAgent == null || userAgent.isEmpty() || userAgent.length() > 500) {
            return false;
        }
        
        userAgent = userAgent.toLowerCase();
        
        // 移动设备关键词
        String[] mobileKeywords = {
            "mobile", "android", "iphone", "ipad", "ipod",
            "blackberry", "windows phone", "webos"
        };
        
        for (String keyword : mobileKeywords) {
            if (userAgent.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
}
