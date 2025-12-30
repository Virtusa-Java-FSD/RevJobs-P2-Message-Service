package com.revjobs.message.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI messageServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Message Service API")
                        .description("REST API for Real-time Messaging and WebSocket Communication")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("RevJobs Team")
                                .email("vishnuisukapatla3@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
