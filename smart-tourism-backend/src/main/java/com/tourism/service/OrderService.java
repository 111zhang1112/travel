package com.tourism.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.common.BusinessException;
import com.tourism.common.PageResult;
import com.tourism.dto.CreateOrderRequest;
import com.tourism.entity.HotelRoom;
import com.tourism.entity.Order;
import com.tourism.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 订单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService extends ServiceImpl<OrderMapper, Order> {

    private final PointsService pointsService;
    private final HotelRoomService hotelRoomService;
    private final TravelRouteService travelRouteService;

    /**
     * 创建订单
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        log.info("[OrderService] 开始创建订单 - productType: {}, productId: {}, productName: {}, amount: {}, quantity: {}", 
                request.getProductType(), request.getProductId(), request.getProductName(), 
                request.getAmount(), request.getQuantity());
        
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(StpUtil.getLoginIdAsLong());
        order.setProductType(request.getProductType());
        order.setProductId(request.getProductId());
        order.setProductName(request.getProductName());
        order.setAmount(request.getAmount());
        order.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);
        
        log.info("[OrderService] 订单号生成: {}, userId: {}", order.getOrderNo(), order.getUserId());
        
        // 酒店预订相关字段
        if ("HOTEL".equals(request.getProductType())) {
            log.info("[OrderService] 处理酒店订单 - roomId: {}, checkInDate: {}, checkOutDate: {}, nights: {}", 
                    request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate(), request.getNights());
            
            order.setRoomId(request.getRoomId());
            order.setCheckInDate(request.getCheckInDate());
            order.setCheckOutDate(request.getCheckOutDate());
            order.setNights(request.getNights());
            
            // 检查并减少可用房间数
            HotelRoom room = hotelRoomService.getById(request.getRoomId());
            if (room == null) {
                log.error("[OrderService] 房型不存在 - roomId: {}", request.getRoomId());
                throw new BusinessException(404, "房型不存在");
            }
            
            Integer availableRooms = room.getAvailableRooms();
            if (availableRooms == null || availableRooms < order.getQuantity()) {
                log.error("[OrderService] 房间数量不足 - roomId: {}, 需要: {}, 可用: {}", 
                        request.getRoomId(), order.getQuantity(), availableRooms);
                throw new BusinessException(400, "房间数量不足，当前可用房间数：" + (availableRooms != null ? availableRooms : 0));
            }
            // 减少可用房间数
            room.setAvailableRooms(availableRooms - order.getQuantity());
            hotelRoomService.updateById(room);
            log.info("[OrderService] 酒店订单创建成功 - 房型ID: {}, 预订数量: {}, 剩余可用房间数: {}", 
                    request.getRoomId(), order.getQuantity(), room.getAvailableRooms());
        }
        
        // 路线预订不涉及库存扣减，但需要验证路线是否存在
        if ("ROUTE".equals(request.getProductType())) {
            log.info("[OrderService] 处理路线订单 - routeId: {}, routeName: {}", 
                    request.getProductId(), request.getProductName());
            
            // 验证路线是否存在
            com.tourism.entity.TravelRoute route = travelRouteService.getById(request.getProductId());
            if (route == null) {
                log.error("[OrderService] 路线不存在 - routeId: {}", request.getProductId());
                throw new BusinessException(404, "路线不存在");
            }
            
            // 验证路线状态
            if (route.getStatus() != null && route.getStatus() == 0) {
                log.error("[OrderService] 路线已下架 - routeId: {}, status: {}", 
                        request.getProductId(), route.getStatus());
                throw new BusinessException(400, "路线已下架，无法预订");
            }
            
            log.info("[OrderService] 路线验证通过 - routeId: {}, routeName: {}, status: {}", 
                    route.getId(), route.getName(), route.getStatus());
        }
        
        // 景点订单
        if ("SCENIC".equals(request.getProductType())) {
            log.info("[OrderService] 处理景点订单 - scenicId: {}, scenicName: {}", 
                    request.getProductId(), request.getProductName());
        }
        
        order.setStatus("PENDING");
        save(order);
        
        log.info("[OrderService] 订单创建成功 - orderNo: {}, productType: {}, status: {}", 
                order.getOrderNo(), order.getProductType(), order.getStatus());
        
        return order;
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
     * 支付订单（模拟）
     */
    @Transactional
    public Order payOrder(String orderNo) {
        Order order = getByOrderNo(orderNo);
        validateOrderOwner(order);

        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(400, "订单状态不允许支付");
        }

        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        updateById(order);
        
        // 支付成功后奖励积分（每消费1元获得1积分）
        pointsService.rewardOrderPoints(order.getUserId(), order.getId(), order.getAmount());
        
        return order;
    }

    /**
     * 取消订单
     */
    @Transactional
    public Order cancelOrder(String orderNo) {
        Order order = getByOrderNo(orderNo);
        validateOrderOwner(order);

        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(400, "只能取消待支付的订单");
        }

        order.setStatus("CANCELLED");
        updateById(order);
        
        // 如果是酒店订单，恢复可用房间数
        if ("HOTEL".equals(order.getProductType()) && order.getRoomId() != null) {
            HotelRoom room = hotelRoomService.getById(order.getRoomId());
            if (room != null) {
                Integer availableRooms = room.getAvailableRooms() != null ? room.getAvailableRooms() : 0;
                room.setAvailableRooms(availableRooms + order.getQuantity());
                hotelRoomService.updateById(room);
                log.info("订单取消，恢复房间数，房型ID: {}, 恢复数量: {}, 当前可用房间数: {}", 
                        order.getRoomId(), order.getQuantity(), room.getAvailableRooms());
            }
        }
        
        return order;
    }
    
    /**
     * 退款订单
     */
    @Transactional
    public Order refundOrder(String orderNo, String reason) {
        Order order = getByOrderNo(orderNo);
        
        if (!"PAID".equals(order.getStatus())) {
            throw new BusinessException(400, "只能退款已支付的订单");
        }
        
        order.setStatus("REFUNDED");
        updateById(order);
        
        // 如果是酒店订单，恢复可用房间数
        if ("HOTEL".equals(order.getProductType()) && order.getRoomId() != null) {
            HotelRoom room = hotelRoomService.getById(order.getRoomId());
            if (room != null) {
                Integer availableRooms = room.getAvailableRooms() != null ? room.getAvailableRooms() : 0;
                room.setAvailableRooms(availableRooms + order.getQuantity());
                hotelRoomService.updateById(room);
                log.info("订单退款，恢复房间数，房型ID: {}, 恢复数量: {}, 当前可用房间数: {}", 
                        order.getRoomId(), order.getQuantity(), room.getAvailableRooms());
            }
        }
        
        return order;
    }

    /**
     * 完成订单（管理员）
     */
    @Transactional
    public Order completeOrder(String orderNo) {
        Order order = getByOrderNo(orderNo);

        if (!"PAID".equals(order.getStatus())) {
            throw new BusinessException(400, "只能完成已支付的订单");
        }

        order.setStatus("COMPLETED");
        updateById(order);
        
        // 积分已在支付时奖励，完成订单不再重复奖励
        
        return order;
    }

    /**
     * 根据订单号查询
     */
    public Order getByOrderNo(String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        Order order = getOne(wrapper);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return order;
    }

    /**
     * 用户订单列表
     */
    public PageResult<Order> getUserOrders(Integer page, Integer size, String status) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);

        if (StringUtils.hasText(status)) {
            wrapper.eq(Order::getStatus, status);
        }

        // 按创建时间降序，如果时间相同则按ID降序（确保最新的订单在最前面）
        wrapper.orderByDesc(Order::getCreateTime);
        wrapper.orderByDesc(Order::getId);
        Page<Order> result = page(pageParam, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 管理端订单列表
     */
    public PageResult<Order> getAdminOrders(Integer page, Integer size, String status, String orderNo) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(status)) {
            wrapper.eq(Order::getStatus, status);
        }
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(Order::getOrderNo, orderNo);
        }

        // 按创建时间降序，如果时间相同则按ID降序（确保最新的订单在最前面）
        wrapper.orderByDesc(Order::getCreateTime);
        wrapper.orderByDesc(Order::getId);
        Page<Order> result = page(pageParam, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 获取总收入
     */
    public BigDecimal getTotalRevenue() {
        return baseMapper.getTotalRevenue();
    }

    /**
     * 按状态统计订单数
     */
    public Long countByStatus(String status) {
        return baseMapper.countByStatus(status);
    }

    /**
     * 获取待处理订单数
     */
    public Long getPendingOrderCount() {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, "PENDING");
        return count(wrapper);
    }

    private void validateOrderOwner(Order order) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (!order.getUserId().equals(currentUserId)) {
            throw new BusinessException(403, "无权操作此订单");
        }
    }

    /**
     * 获取订单趋势（最近12个月）
     */
    public java.util.List<java.util.Map<String, Object>> getOrderTrend() {
        return baseMapper.getOrderTrend();
    }

    /**
     * 获取订单类型分布
     */
    public java.util.List<java.util.Map<String, Object>> getOrderTypeDistribution() {
        return baseMapper.getOrderTypeDistribution();
    }

    /**
     * 自动完成已支付超过指定天数的订单
     * @param days 天数
     * @return 更新的订单数量
     */
    @Transactional
    public int autoCompleteOrders(int days) {
        return baseMapper.autoCompleteOrders(days);
    }
    
    /**
     * 获取今日订单数
     */
    public Long getTodayOrderCount() {
        return baseMapper.getTodayOrderCount();
    }
    
    /**
     * 获取今日收入
     */
    public BigDecimal getTodayRevenue() {
        return baseMapper.getTodayRevenue();
    }
    
    /**
     * 获取收入趋势
     * @param period 时间周期：day(最近7天), week(最近12周), month(最近12个月)
     */
    public java.util.List<java.util.Map<String, Object>> getRevenueTrend(String period) {
        switch (period) {
            case "day":
                return baseMapper.getRevenueTrendByDay();
            case "week":
                return baseMapper.getRevenueTrendByWeek();
            case "month":
            default:
                return baseMapper.getRevenueTrendByMonth();
        }
    }
    
    /**
     * 获取订单状态分布
     */
    public java.util.List<java.util.Map<String, Object>> getOrderStatusDistribution() {
        return baseMapper.getOrderStatusDistribution();
    }
    
    /**
     * 商家订单列表（根据商家ID查询）
     */
    public PageResult<Order> getMerchantOrders(Long merchantId, Integer page, Integer size, String status, String orderNo) {
        Page<Order> pageParam = new Page<>(page, size);
        
        // 查询该商家的所有产品订单
        Page<Order> result = baseMapper.getMerchantOrders(pageParam, merchantId, status, orderNo);
        
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }
    
    /**
     * 商家获取订单详情（需验证权限）
     */
    public Order getMerchantOrderDetail(Long merchantId, String orderNo) {
        Order order = getByOrderNo(orderNo);
        
        // 验证订单是否属于该商家的产品
        boolean hasAccess = baseMapper.checkMerchantOrderAccess(merchantId, order.getId());
        if (!hasAccess) {
            throw new BusinessException(403, "无权访问此订单");
        }
        
        return order;
    }
    
    /**
     * 商家订单统计
     */
    public java.util.Map<String, Object> getMerchantOrderStats(Long merchantId) {
        return baseMapper.getMerchantOrderStats(merchantId);
    }
}
