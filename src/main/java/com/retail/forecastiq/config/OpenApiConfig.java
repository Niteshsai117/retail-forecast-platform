package com.retail.forecastiq.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI retailForecastOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Retail ForecastIQ API")
                        .description("Demand forecasting and inventory optimization platform for retail operations. " +
                                "Supports Simple Moving Average (SMA) and Weighted Moving Average (WMA) algorithms.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ForecastIQ Team")
                                .email("support@forecastiq.retail"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
