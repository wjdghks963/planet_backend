package com.jung.planet.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI openAPI() {
        String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .addServersItem(new Server().url("/"))
                .info(new Info()
                        .title("Planet API")
                        .description("Planet 백엔드 API 문서\n\n" +
                                "## 오류 코드\n\n" +
                                "| 코드 | 설명 | HTTP 상태 코드 |\n" +
                                "| --- | --- | --- |\n" +
                                "| resource.not.found | 요청한 리소스를 찾을 수 없습니다 | 404 |\n" +
                                "| authentication.failed | 인증에 실패했습니다 | 401 |\n" +
                                "| permission.denied | 접근 권한이 없습니다 | 403 |\n" +
                                "| unauthorized.action | 권한이 없는 작업입니다 | 403 |\n" +
                                "| bad.request | 잘못된 요청입니다 | 400 |\n" +
                                "| validation.failed | 입력값 검증에 실패했습니다 | 400 |\n" +
                                "| server.error | 서버 내부 오류가 발생했습니다 | 500 |\n" +
                                "| external.service.error | 외부 서비스 연동 중 오류가 발생했습니다 | 500 |\n")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Planet Team")
                                .email("planet@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, 
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
} 