package com.fyf.leyou.queue;

import java.util.UUID;

import javax.annotation.PostConstruct;

import com.fyf.leyou.dao.OrderDao;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Sender implements RabbitTemplate.ConfirmCallback, ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    private OrderDao orderDao;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            System.out.println("消息发送成功:" + correlationData);
            //删除消息表
            orderDao.deleteMessage(correlationData.getId());
        } else {
            System.out.println("消息发送失败:" + cause);
        }
    }

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println(message.getMessageProperties() + " 发送失败");

    }

    //发送消息，不需要实现任何接口，供外部调用。
    public void send(String msg){
        CorrelationData correlationId = new CorrelationData(msg);
         rabbitTemplate.convertAndSend("topicExchange", "storageOrder_key", msg, correlationId);
    }


}

