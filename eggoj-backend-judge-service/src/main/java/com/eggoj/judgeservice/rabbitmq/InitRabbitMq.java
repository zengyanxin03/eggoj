package com.eggoj.judgeservice.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InitRabbitMq {

    public static void doInit(){
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            // 创建一个连接,与服务器建立一个连接
            Connection connection = factory.newConnection();
            // 操作消息队列的客户端
            Channel channel = connection.createChannel();
            // 创建一个交换机
            String EXCHANGE_NAME = "eggoj_exchange";
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            // 创建一个队列
            String queueName = "eggoj_queue";
            channel.queueDeclare(queueName,true,false,false,null);
            channel.queueBind(queueName, EXCHANGE_NAME, "eggoj_routingKey");
            log.info("消息队列启动成功");
        }catch (Exception e){
            log.error("消息队列启动失败");
        }
    }

    public static void main(String[] args) {
        doInit();
    }
}
