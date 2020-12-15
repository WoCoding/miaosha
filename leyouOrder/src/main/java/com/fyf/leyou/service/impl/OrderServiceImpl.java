package com.fyf.leyou.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fyf.leyou.dao.OrderDao;
import com.fyf.leyou.queue.Sender;
import com.fyf.leyou.service.OrderService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Sender sender;
    /*@PostConstruct
    public void init(){
        List<Map<String, Object>> stockList =restTemplate.getForObject("http://leyouStockServer/getStockList", List.class);
        for(Map stockMap:stockList){
            String sku_id = stockMap.get("sku_id").toString();
            String stock= stockMap.get("stock").toString();
            stringRedisTemplate.opsForValue().set("LIMIT_POLICY_"+sku_id,stock);
        }

    }*/
    //创建订单
    @Override
    @Transactional(rollbackFor=Exception.class)
    public Map<String, Object> createOrder(String sku_id, String user_id) {
        Map<String, Object> resultMap = new HashMap<>();
        Integer order_id =0;
        //判断sku_id
        if (sku_id == null || "".equals(sku_id)) {
            resultMap.put("result", false);
            resultMap.put("msg", "sku_id为空！");
            return resultMap;
        }
        //分布式锁方案
       /* String lockKey=sku_id;
        try{
            //加锁
            Boolean result=stringRedisTemplate.opsForValue().setIfAbsent(lockKey,user_id,30, TimeUnit.SECONDS);
            if(!result) {
                resultMap.put("result", false);
                resultMap.put("msg", "获取锁失败！");
                return resultMap;
            }
            int stock=Integer.parseInt(stringRedisTemplate.opsForValue().get("stock"));
            if(stock>0){
                int realStock=stock-1;
                stringRedisTemplate.opsForValue().set("stock",realStock+"");
            }else {
                resultMap.put("result", false);
                resultMap.put("msg", "库存不足！");
                return resultMap;
            }
        }finally {
            if(user_id.equals(stringRedisTemplate.opsForValue().get(lockKey))){
                stringRedisTemplate.delete(lockKey);
            }
        }*
        */
       //计数器方案
        //从redis中取出该商品的秒杀政策
        //{'sku_id':'26816294479','quanty':1000,'price':1000,'begin_time':'2020-07-29 13:00','end_time':'2020-7-30 12:00'}
        String policy = stringRedisTemplate.opsForValue().get("LIMIT_POLICY_" + sku_id);

        if (policy != null && !" ".equals(policy)) {
            //JSON转换为map集合
            Map<String, Object> policyMap = JSONObject.parseObject(policy, Map.class);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String now = restTemplate.getForObject("http://leyouTimeServer/getTime", String.class);
            try {
                //获取开始结束时间
                Date begin_time = simpleDateFormat.parse(policyMap.get("begin_time").toString());
                Date end_time = simpleDateFormat.parse(policyMap.get("end_time").toString());
                Date now_time = simpleDateFormat.parse(now);
                //秒杀已经开始，还未结束
                if (begin_time.getTime() <= now_time.getTime() && now_time.getTime() <= end_time.getTime()) {
                    //从政策中 获取可以秒杀的数量
                    Long limitQuanty = Long.valueOf(policyMap.get("quanty").toString());
                    //创建redis计数器,限制1000人进入，每进入一人+1
                    if (stringRedisTemplate.opsForValue().increment("SKU_QUANTY_" + sku_id, 1) <= limitQuanty) {

                        String sku = stringRedisTemplate.opsForValue().get("SKU_" + sku_id);
                        JSONObject skuMap = JSONObject.parseObject(sku);
                        Map<String, Object> orderMap = new HashMap<>();
                        orderMap.put("total_fee", skuMap.get("price"));
                        orderMap.put("actual_fee", policyMap.get("price"));
                        orderMap.put("post_fee", 0);
                        orderMap.put("payment_type", 0);
                        orderMap.put("user_id", user_id);
                        orderMap.put("status", 1);
                        orderMap.put("create_time", now);

                        orderMap.put("sku_id", skuMap.get("sku_id"));
                        orderMap.put("num", 1);
                        orderMap.put("title", skuMap.get("title"));
                        orderMap.put("own_spec", skuMap.get("own_spec"));
                        orderMap.put("price", policyMap.get("price"));
                        orderMap.put("image", skuMap.get("images"));
                        //生成订单
                        orderDao.addOrder(orderMap);
                        order_id= (Integer) orderMap.get("order_id");
                        //插入库存信息表
                        orderDao.addMessage(sku_id);
                        //发送sku_id到storage_queue队列中
                        try {
                            sender.send(sku_id);
                        } catch (Exception e) {
                            resultMap.put("result", false);
                            resultMap.put("msg", "写入队列异常！");
                            return resultMap;
                        }

                    }
                    else {
                        resultMap.put("result", false);
                        resultMap.put("msg", "商品已售完！");
                        return resultMap;
                    }
                }
                //秒杀结束或者还未开始
                else {
                    resultMap.put("result", false);
                    resultMap.put("msg", "秒杀结束或者还未开始！");
                    return resultMap;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            //没有取出政策，表示redis中的政策已经过期
            resultMap.put("result", false);
            resultMap.put("msg", "活动已经过期！");
            return resultMap;
        }

        //下单成功，返回正常数据，跳到支付页面
        resultMap.put("result", true);
        resultMap.put("msg", "");
        resultMap.put("order_id", order_id);
        return resultMap;
    }

    @Override
    @Transactional
    public Map<String, Object> addOrder(Map<String, Object> orderMap) {
        Map<String, Object> resultMap = new HashMap<>();
        if (orderMap == null || orderMap.isEmpty()) {
            resultMap.put("result", false);
            resultMap.put("msg", "传入参数有误！");
            return resultMap;
        }
        Integer result = orderDao.addOrder(orderMap);
        Integer result1 = orderDao.addOrderDetail(orderMap);
        if (result == 0 || result1 == 0) {
            resultMap.put("result", false);
            resultMap.put("msg", "订单写入失败！");
            return resultMap;
        }
        resultMap.put("result", true);
        resultMap.put("msg", "");
        return resultMap;


    }

    @Override
    public Map<String, Object> payOrder(String order_id, String sku_id) {
        Map<String, Object> resultMap = new HashMap<>();
        if (order_id == null || order_id.equals("")) {
            resultMap.put("result", false);
            resultMap.put("msg", "订单有误！");
            return resultMap;
        }
        //正常情况下在这里会调用支付接口，我们这里模拟支付已经返回正常数据
        boolean isPay = true;
        //支付完后更新订单状态为2
        Integer result = orderDao.updateOrderStatus(order_id);
        if (result == 0) {
            resultMap.put("result", false);
            resultMap.put("msg", "订单状态更新失败！");
            return resultMap;
        }

        resultMap.put("result", true);
        resultMap.put("msg", "");
        return resultMap;


    }

    @Override
    public Map<String, Object> getOrder(String order_id) {
        Map<String, Object> resultMap = new HashMap<>();
        if (order_id==null||order_id.equals("")){
            resultMap.put("result", false);
            resultMap.put("msg", "参数传入有误！");
            return resultMap;
        }
        Map<String, Object> orderMap = orderDao.getOrder(order_id);
        if(orderMap == null || orderMap.isEmpty()){
            resultMap.put("result", false);
            resultMap.put("msg", "未找到订单信息！");
            return resultMap;
        }
        resultMap=orderMap;
        resultMap.put("result", true);
        resultMap.put("msg", "");
        return resultMap;
    }
}
