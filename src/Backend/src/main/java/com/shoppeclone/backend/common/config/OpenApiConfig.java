package com.shoppeclone.backend.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Shoppe Clone API")
                        .version("1.0.0")
                        .description("""
                                API documentation cho hệ thống e-commerce Shoppe Clone.

                                **Modules**: Auth, User, Shop, Product, Cart, Order, Payment,
                                Shipping, Promotion (Voucher + Flash Sale), Review, Refund,
                                Dispute, Chat, Search, Admin, Notification, Follow.

                                **Auth**: Gửi JWT token trong header `Authorization: Bearer <token>`
                                """)
                        .contact(new Contact()
                                .name("Shoppe Clone Team")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập JWT token (không cần prefix 'Bearer')")));
    }
}
