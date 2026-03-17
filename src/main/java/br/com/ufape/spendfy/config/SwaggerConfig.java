package br.com.ufape.spendfy.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("SpendFy API")
                .version("1.0.0")
                .description("API para gestão financeira pessoal - Personal Finance Management API")
                .contact(new Contact()
                    .name("SpendFy Team")
                    .email("support@spendfy.com")
                    .url("https://github.com/SpendFy"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT")));
    }
}