package com.fyf.leyou.queue;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {
    @Bean
    public Queue getstorageQueue(){
        return new Queue("storage_queue",true);
    }
    //交换机
    @Bean
    TopicExchange topicExchange() {
        return new TopicExchange("topicExchange");
    }
    //绑定
    @Bean
    public Binding binding1() {
        return BindingBuilder.bind(getstorageQueue()).to(topicExchange()).with("storageOrder_key");
    }

}
