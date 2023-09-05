package com.wibmo.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import springfox.documentation.swagger2.annotations.EnableSwagger2;


@EnableJpaRepositories("com.wibmo.repository")
@EntityScan("com.wibmo.entity")
@EnableWebMvc
@EnableSwagger2
@EnableAutoConfiguration
@Configuration
@ComponentScan("com.wibmo.*")
@EnableEurekaClient
@SpringBootApplication
public class CrsGroupAWibmoPaymentsMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrsGroupAWibmoPaymentsMicroserviceApplication.class, args);
	}
}
