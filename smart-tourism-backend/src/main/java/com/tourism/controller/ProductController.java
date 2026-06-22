package com.tourism.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.Result;
import com.tourism.entity.Product;
import com.tourism.entity.ProductPurchase;
import com.tourism.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    /**
     * 获取商品列表
     */
    @GetMapping
    public Result<Page<Product>> getProductList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(productService.getProductList(page, size, keyword));
    }
    
    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Long id) {
        return Result.success(productService.getProductById(id));
    }
    
    /**
     * 购买商品
     */
    @PostMapping("/purchase")
    public Result<ProductPurchase> purchaseProduct(@RequestBody PurchaseRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(productService.purchaseProduct(
                userId,
                request.getProductId(),
                request.getQuantity()
        ));
    }
    
    /**
     * 获取我的购买记录
     */
    @GetMapping("/purchases")
    public Result<List<ProductPurchase>> getMyPurchases() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(productService.getUserPurchases(userId));
    }
    
    /**
     * 购买请求
     */
    @lombok.Data
    public static class PurchaseRequest {
        private Long productId;
        private Integer quantity = 1;
    }
}
