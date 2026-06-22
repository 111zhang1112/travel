package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单 Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    @Select("SELECT COALESCE(SUM(amount), 0) FROM orders WHERE status IN ('PAID', 'COMPLETED')")
    BigDecimal getTotalRevenue();
    
    @Select("SELECT COUNT(*) FROM orders WHERE status = #{status}")
    Long countByStatus(String status);
    
    /**
     * 获取最近12个月的订单趋势
     */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') as month, COUNT(*) as count " +
            "FROM orders " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m') " +
            "ORDER BY month ASC")
    List<Map<String, Object>> getOrderTrend();
    
    /**
     * 获取订单类型分布
     */
    @Select("SELECT product_type as type, COUNT(*) as count " +
            "FROM orders " +
            "GROUP BY product_type")
    List<Map<String, Object>> getOrderTypeDistribution();

    /**
     * 自动完成已支付超过指定天数的订单
     * @param days 天数
     * @return 更新的订单数量
     */
    @Update("UPDATE orders SET status = 'COMPLETED', update_time = NOW() " +
            "WHERE status = 'PAID' AND pay_time <= DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    int autoCompleteOrders(int days);
    
    /**
     * 获取今日订单数
     */
    @Select("SELECT COUNT(*) FROM orders WHERE DATE(create_time) = CURDATE()")
    Long getTodayOrderCount();
    
    /**
     * 获取今日收入
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM orders " +
            "WHERE DATE(create_time) = CURDATE() AND status IN ('PAID', 'COMPLETED')")
    BigDecimal getTodayRevenue();
    
    /**
     * 获取最近7天的收入趋势
     */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') as date, " +
            "COALESCE(SUM(amount), 0) as revenue " +
            "FROM orders " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "AND status IN ('PAID', 'COMPLETED') " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d') " +
            "ORDER BY date ASC")
    List<Map<String, Object>> getRevenueTrendByDay();
    
    /**
     * 获取最近12周的收入趋势
     */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%u') as week, " +
            "COALESCE(SUM(amount), 0) as revenue " +
            "FROM orders " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 12 WEEK) " +
            "AND status IN ('PAID', 'COMPLETED') " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%u') " +
            "ORDER BY week ASC")
    List<Map<String, Object>> getRevenueTrendByWeek();
    
    /**
     * 获取最近12个月的收入趋势
     */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') as month, " +
            "COALESCE(SUM(amount), 0) as revenue " +
            "FROM orders " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
            "AND status IN ('PAID', 'COMPLETED') " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m') " +
            "ORDER BY month ASC")
    List<Map<String, Object>> getRevenueTrendByMonth();
    
    /**
     * 获取订单状态分布
     */
    @Select("SELECT status, COUNT(*) as count FROM orders GROUP BY status")
    List<Map<String, Object>> getOrderStatusDistribution();
    
    /**
     * 商家订单列表（通过product_id关联到商家）
     */
    @Select("<script>" +
            "SELECT o.* FROM orders o " +
            "LEFT JOIN scenic_spot s ON o.product_type = 'SCENIC' AND o.product_id = s.id " +
            "LEFT JOIN hotel h ON o.product_type = 'HOTEL' AND o.product_id = h.id " +
            "WHERE (s.merchant_id = #{merchantId} OR h.merchant_id = #{merchantId}) " +
            "<if test='status != null and status != \"\"'>" +
            "AND o.status = #{status} " +
            "</if>" +
            "<if test='orderNo != null and orderNo != \"\"'>" +
            "AND o.order_no LIKE CONCAT('%', #{orderNo}, '%') " +
            "</if>" +
            "ORDER BY o.create_time DESC, o.id DESC" +
            "</script>")
    Page<Order> getMerchantOrders(Page<Order> page, Long merchantId, String status, String orderNo);
    
    /**
     * 检查商家是否有权访问该订单
     */
    @Select("SELECT COUNT(*) > 0 FROM orders o " +
            "LEFT JOIN scenic_spot s ON o.product_type = 'SCENIC' AND o.product_id = s.id " +
            "LEFT JOIN hotel h ON o.product_type = 'HOTEL' AND o.product_id = h.id " +
            "WHERE o.id = #{orderId} AND (s.merchant_id = #{merchantId} OR h.merchant_id = #{merchantId})")
    boolean checkMerchantOrderAccess(Long merchantId, Long orderId);
    
    /**
     * 商家订单统计
     */
    @Select("SELECT " +
            "COUNT(*) as totalOrders, " +
            "COUNT(CASE WHEN o.status = 'PENDING' THEN 1 END) as pendingOrders, " +
            "COUNT(CASE WHEN o.status = 'PAID' THEN 1 END) as paidOrders, " +
            "COUNT(CASE WHEN o.status = 'COMPLETED' THEN 1 END) as completedOrders, " +
            "COUNT(CASE WHEN o.status = 'CANCELLED' THEN 1 END) as cancelledOrders, " +
            "COUNT(CASE WHEN o.status = 'REFUNDED' THEN 1 END) as refundedOrders, " +
            "COALESCE(SUM(CASE WHEN o.status IN ('PAID', 'COMPLETED') THEN o.amount ELSE 0 END), 0) as totalRevenue, " +
            "COUNT(CASE WHEN DATE(o.create_time) = CURDATE() THEN 1 END) as todayOrders, " +
            "COALESCE(SUM(CASE WHEN DATE(o.create_time) = CURDATE() AND o.status IN ('PAID', 'COMPLETED') THEN o.amount ELSE 0 END), 0) as todayRevenue " +
            "FROM orders o " +
            "LEFT JOIN scenic_spot s ON o.product_type = 'SCENIC' AND o.product_id = s.id " +
            "LEFT JOIN hotel h ON o.product_type = 'HOTEL' AND o.product_id = h.id " +
            "WHERE (s.merchant_id = #{merchantId} OR h.merchant_id = #{merchantId})")
    Map<String, Object> getMerchantOrderStats(Long merchantId);
    
    /**
     * 商家订单趋势（最近12个月）
     */
    @Select("SELECT DATE_FORMAT(o.create_time, '%Y-%m') as month, COUNT(*) as orderCount " +
            "FROM orders o " +
            "LEFT JOIN scenic_spot s ON o.product_type = 'SCENIC' AND o.product_id = s.id " +
            "LEFT JOIN hotel h ON o.product_type = 'HOTEL' AND o.product_id = h.id " +
            "WHERE (s.merchant_id = #{merchantId} OR h.merchant_id = #{merchantId}) " +
            "AND o.create_time >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
            "GROUP BY DATE_FORMAT(o.create_time, '%Y-%m') " +
            "ORDER BY month ASC")
    List<Map<String, Object>> getMerchantOrderTrend(Long merchantId);
    
    /**
     * 商家收入趋势（最近7天）
     */
    @Select("SELECT DATE_FORMAT(o.create_time, '%Y-%m-%d') as date, " +
            "COALESCE(SUM(o.amount), 0) as revenue " +
            "FROM orders o " +
            "LEFT JOIN scenic_spot s ON o.product_type = 'SCENIC' AND o.product_id = s.id " +
            "LEFT JOIN hotel h ON o.product_type = 'HOTEL' AND o.product_id = h.id " +
            "WHERE (s.merchant_id = #{merchantId} OR h.merchant_id = #{merchantId}) " +
            "AND o.create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "AND o.status IN ('PAID', 'COMPLETED') " +
            "GROUP BY DATE_FORMAT(o.create_time, '%Y-%m-%d') " +
            "ORDER BY date ASC")
    List<Map<String, Object>> getMerchantRevenueTrendByDay(Long merchantId);
    
    /**
     * 商家收入趋势（最近12周）
     */
    @Select("SELECT DATE_FORMAT(o.create_time, '%Y-%u') as week, " +
            "COALESCE(SUM(o.amount), 0) as revenue " +
            "FROM orders o " +
            "LEFT JOIN scenic_spot s ON o.product_type = 'SCENIC' AND o.product_id = s.id " +
            "LEFT JOIN hotel h ON o.product_type = 'HOTEL' AND o.product_id = h.id " +
            "WHERE (s.merchant_id = #{merchantId} OR h.merchant_id = #{merchantId}) " +
            "AND o.create_time >= DATE_SUB(CURDATE(), INTERVAL 12 WEEK) " +
            "AND o.status IN ('PAID', 'COMPLETED') " +
            "GROUP BY DATE_FORMAT(o.create_time, '%Y-%u') " +
            "ORDER BY week ASC")
    List<Map<String, Object>> getMerchantRevenueTrendByWeek(Long merchantId);
    
    /**
     * 商家收入趋势（最近12个月）
     */
    @Select("SELECT DATE_FORMAT(o.create_time, '%Y-%m') as month, " +
            "COALESCE(SUM(o.amount), 0) as revenue " +
            "FROM orders o " +
            "LEFT JOIN scenic_spot s ON o.product_type = 'SCENIC' AND o.product_id = s.id " +
            "LEFT JOIN hotel h ON o.product_type = 'HOTEL' AND o.product_id = h.id " +
            "WHERE (s.merchant_id = #{merchantId} OR h.merchant_id = #{merchantId}) " +
            "AND o.create_time >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
            "AND o.status IN ('PAID', 'COMPLETED') " +
            "GROUP BY DATE_FORMAT(o.create_time, '%Y-%m') " +
            "ORDER BY month ASC")
    List<Map<String, Object>> getMerchantRevenueTrendByMonth(Long merchantId);
    
    /**
     * 商家产品销量排名
     */
    @Select("SELECT o.product_name as productName, o.product_type as productType, " +
            "COUNT(*) as orderCount, " +
            "COALESCE(SUM(o.amount), 0) as totalRevenue " +
            "FROM orders o " +
            "LEFT JOIN scenic_spot s ON o.product_type = 'SCENIC' AND o.product_id = s.id " +
            "LEFT JOIN hotel h ON o.product_type = 'HOTEL' AND o.product_id = h.id " +
            "WHERE (s.merchant_id = #{merchantId} OR h.merchant_id = #{merchantId}) " +
            "AND o.status IN ('PAID', 'COMPLETED') " +
            "GROUP BY o.product_id, o.product_name, o.product_type " +
            "ORDER BY orderCount DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getMerchantProductRanking(Long merchantId, Integer limit);
}
