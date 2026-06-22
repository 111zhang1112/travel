package com.tourism.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.entity.Product;
import com.tourism.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理后台 - 商品管理控制器
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {
    
    private final ProductService productService;
    
    /**
     * 获取商品列表（包含下架商品）
     */
    @GetMapping
    public Page<Product> getProductList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return productService.getAllProducts(page, size);
    }
    
    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }
    
    /**
     * 添加商品
     */
    @PostMapping
    public void addProduct(@RequestBody Product product) {
        productService.addProduct(product);
    }
    
    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public void updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        productService.updateProduct(product);
    }
    
    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}
