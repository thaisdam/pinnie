package com.pinnie.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pinnieOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Pinnie API")
                        .description("API REST para a aplicação Pinnie (Clone do Pinterest). Inclui autenticação segura via cookies, paginação via Slice e upload em duas etapas.")
                        .version("1.0.0"));
    }
}
