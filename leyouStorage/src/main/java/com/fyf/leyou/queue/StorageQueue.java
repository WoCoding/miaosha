package com.fyf.leyou.queue;

import com.fyf.leyou.service.StorageService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
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


    //监听storage_queue队列,获取消息，执行方法
    @RabbitListener(queues = "storage_queue")
    public void updateStorage(Channel channel, @Payload String message, @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                              @Header(AmqpHeaders.REDELIVERED) boolean reDelivered ) throws IOException {

            //输出消息
            System.out.println("接收storage_queue消息："+message);
            //根据msg,进行更新库存
            Map<String,Object> resultMap=new HashMap<>();
            resultMap=storageService.insertStorage(message,0,1);
            //判断是否处理成功
            if((Boolean) resultMap.get("result")){
                System.out.println("消息处理成功！");
                //手动确认
                channel.basicAck(tag, false);
            }else {
                System.out.println("storage_queue消息处理失败："+resultMap.get("msg").toString());
                //重新入队一次
                channel.basicNack(tag,false,true);
            }
    }

}
