package com.tourism.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.entity.Product;
import com.tourism.entity.ProductPurchase;
import com.tourism.mapper.ProductMapper;
import com.tourism.mapper.ProductPurchaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品服务
 */
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductMapper productMapper;
    private final ProductPurchaseMapper productPurchaseMapper;
    
    /**
     * 分页查询商品列表
     */
    public Page<Product> getProductList(int page, int size, String keyword) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);  // 只查询上架商品
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getName, keyword);
        }
        
        wrapper.orderByDesc(Product::getCreateTime);
        return productMapper.selectPage(pageParam, wrapper);
    }
    
    /**
     * 获取商品详情
     */
    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }
    
    /**
     * 购买商品
     */
    @Transactional
    public ProductPurchase purchaseProduct(Long userId, Long productId, Integer quantity) {
        // 查询商品
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        if (product.getStatus() != 1) {
            throw new RuntimeException("商品已下架");
        }
        
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }
        
        // 扣减库存
        product.setStock(product.getStock() - quantity);
        productMapper.updateById(product);
        
        // 创建购买记录
        ProductPurchase purchase = new ProductPurchase();
        purchase.setUserId(userId);
        purchase.setProductId(productId);
        purchase.setPricePaid(product.getPrice().multiply(new java.math.BigDecimal(quantity)));
        purchase.setQuantity(quantity);
        purchase.setPurchaseDate(LocalDateTime.now());
        
        productPurchaseMapper.insert(purchase);
        
        return purchase;
    }
    
    /**
     * 获取用户购买记录
     */
    public List<ProductPurchase> getUserPurchases(Long userId) {
        return productPurchaseMapper.selectByUserIdWithDetails(userId);
    }
    
    /**
     * 获取所有商品（管理后台）
     */
    public Page<Product> getAllProducts(int page, int size) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Product::getCreateTime);
        return productMapper.selectPage(pageParam, wrapper);
    }
    
    /**
     * 添加商品
     */
    public void addProduct(Product product) {
        productMapper.insert(product);
    }
    
    /**
     * 更新商品
     */
    public void updateProduct(Product product) {
        productMapper.updateById(product);
    }
    
    /**
     * 删除商品
     */
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }
}
