package com.wibmo.routeconfiguration;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SpringCloudConfiguration {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
    	
    	System.out.println("n router");
        return builder.routes()
        		 .route(r -> r
        				 .path("/api/student/**")
        				 .uri("http://localhost:8083/"))
        		 .route(r -> r
						 .path("/api/professor/**")
						 .uri("http://localhost:8082/"))
				.route(r -> r
						 .path("/api/authentication/**")
						 .uri("http://localhost:8081/"))
				.route(r-> r
						 .path("/api/admin/**")
						 .uri("http://localhost:8084/"))
				.route(r -> r
						.path("/api/payment/**")
						.uri("http://localhost:8085/"))
        		 .build();
    }

}