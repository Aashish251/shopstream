package com.hoangtien2k3.notificationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NOTIFICATION-SERVICE API")
                        .description("API Documentation's NOTIFICATION-SERVICE")
                        .contact(new Contact()
                                .name("hoangtien2k3")
                                .url("https://hoangtien2k3qx1.github.io/")
                                .email("hoangtien2k3qx1@gmail.com"))
                        .version("1.0.0")
                        .license(new io.swagger.v3.oas.models.info.License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
