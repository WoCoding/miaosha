package com.fyf.leyou.queue;

import com.fyf.leyou.service.StorageService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class StorageQueue {
    @Autowired
    private StorageService storageService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //监听storage_queue队列,获取消息，执行方法
    @RabbitListener(queues = "storage_queue")
    public void updateStorage(Channel channel, @Payload String message, @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                              @Header(AmqpHeaders.REDELIVERED) boolean reDelivered) throws IOException {
        try {
            //输出消息
            System.out.println("接收storage_queue消息：" + message);
            //保证幂等性
            if (stringRedisTemplate.opsForValue().get(String.valueOf(tag)) == null) {
                //减库存
                storageService.insertStorage(message, 0, 1);
                stringRedisTemplate.opsForValue().set(String.valueOf(tag), "1");
                channel.basicAck(tag, false);
            }

        } catch (Exception e) {
            if (reDelivered) {
                System.out.println("消息已重复处理失败：{}" + message);
                channel.basicReject(tag, false);
            } else {
                System.out.println("消息处理失败" + e);
                //重新入队一次
                channel.basicNack(tag, false, true);
            }

        }

    }


}
