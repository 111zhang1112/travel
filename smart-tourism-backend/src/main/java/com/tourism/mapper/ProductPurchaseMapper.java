package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.entity.ProductPurchase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品购买记录 Mapper
 */
@Mapper
public interface ProductPurchaseMapper extends BaseMapper<ProductPurchase> {
    
    /**
     * 查询用户的购买记录（包含商品信息）
     */
    @Select("SELECT pp.*, p.name as product_name, p.image_url as product_image, u.username " +
            "FROM product_purchase pp " +
            "LEFT JOIN product p ON pp.product_id = p.id " +
            "LEFT JOIN sys_user u ON pp.user_id = u.id " +
            "WHERE pp.user_id = #{userId} " +
            "ORDER BY pp.purchase_date DESC")
    List<ProductPurchase> selectByUserIdWithDetails(Long userId);
}
