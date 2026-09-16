package org.example.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 路由鉴权配置
 *
 * 路径级粗粒度拦截 + 控制器内方法级细粒度校验（StpUtil.checkLogin / checkRole）。
 * 说明：
 *  - /api/author/apply、/api/author/apply/status 是"读者申请入驻"接口，登录即可，需在路由中排除；
 *  - 其余 /api/author/** 要求角色为作者(1)；
 *  - /api/admin/** 要求角色为管理员(2)；
 *  - 读者端个人接口要求登录。
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 读者端个人操作：需登录
            SaRouter.match("/api/user/**", "/api/bookshelf/**", "/api/order/**", "/api/notification/**")
                    .check(r -> StpUtil.checkLogin());
            // 作者端：需作者角色（排除入驻申请相关接口）
            SaRouter.match("/api/author/**")
                    .notMatch("/api/author/apply", "/api/author/apply/status")
                    .check(r -> StpUtil.checkRole("1"));
            // 管理员端：需管理员角色
            SaRouter.match("/api/admin/**")
                    .check(r -> StpUtil.checkRole("2"));
        })).addPathPatterns("/**");
    }
}
