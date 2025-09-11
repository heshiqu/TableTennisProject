package com.example.ttp_serve.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
// 如果你使用了JWT，可以配置全局安全方案
@SecurityScheme(
        name = "bearerAuth", // 方案名称，可在接口上引用
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // 其他需要自定义的 Bean 可以在这里定义
    // 通常 springdoc-openapi 的自动配置已经足够强大
}