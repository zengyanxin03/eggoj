package com.eggoj.questionservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.eggoj.questionservice.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.eggoj")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.eggoj.serviceclient.service")
public class eggojBackendQuestionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(eggojBackendQuestionServiceApplication.class, args);
    }

}
