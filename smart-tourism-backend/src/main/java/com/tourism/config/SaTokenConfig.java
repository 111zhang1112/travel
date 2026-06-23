package com.tourism.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 放行 OPTIONS 预检请求
            SaRouter.match(cn.dev33.satoken.router.SaHttpMethod.OPTIONS).back();
            // 登录校验 - 排除公开接口
            SaRouter.match("/**")
                    .notMatch(
                            "/api/auth/login",
                            "/api/auth/admin-login",
                            "/api/auth/register",
                            "/api/merchant/auth/login",
                            "/api/merchant/auth/register",
                            "/api/scenic/**",
                            "/api/hotel/**",
                            "/api/guide/list",
                            "/api/guide/detail/**",
                            "/api/recommend/**",
                            "/api/system/**",
                            "/api/community/**",
                            "/api/companion/**",
                            "/api/qa/**",
                            "/api/route/**",
                            "/api/weather/**",
                            "/api/payment/alipay/notify",
                            "/api/upload/**",
                            "/uploads/**",
                            "/images/**",
                            "/ws/**",
                            "/error"
                    )
                    .check(r -> StpUtil.checkLogin());

            // 管理员权限校验
            SaRouter.match("/api/admin/**")
                    .check(r -> StpUtil.checkRole("ADMIN"));
            
            // 商家权限校验 - 排除注册和登录接口
            SaRouter.match("/api/merchant/**")
                    .notMatch("/api/merchant/auth/register", "/api/merchant/auth/login")
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的静态资源映射（使用绝对路径）
        String absolutePath = System.getProperty("user.dir") + "/" + uploadPath + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath);
        
        // 兼容旧的图片路径 /images/scenic/ -> uploads/images/scenic/
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + absolutePath + "images/");
    }
}
