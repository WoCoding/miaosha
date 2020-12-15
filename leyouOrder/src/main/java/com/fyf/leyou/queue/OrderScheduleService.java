package com.fyf.leyou.queue;

import com.fyf.leyou.dao.OrderDao;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderScheduleService {
    @Autowired
    OrderDao orderDao;
    Sender sender;
    // 定时扫描记录表，将未成功发送的消息再次发送
    @Scheduled(cron ="30/30 * * * * ?" )
    public void scanStcok(){
        System.out.println("定时扫描confirm表");
        List<String> message = orderDao.getMessage();
        for (String sku_id:message) {
            sender.send(sku_id);
        }
    }
}
