package com.eggoj.judgeservice;

import com.eggoj.judgeservice.rabbitmq.InitRabbitMq;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.eggoj")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.eggoj.serviceclient.service")
public class eggojBackendJudgeServiceApplication {

    public static void main(String[] args) {
        // 如果消息队列没有启动起来，作为消费端应该第一时间报错
        InitRabbitMq.doInit();
        SpringApplication.run(eggojBackendJudgeServiceApplication.class, args);
    }

}
