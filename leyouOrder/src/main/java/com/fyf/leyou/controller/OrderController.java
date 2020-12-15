package com.fyf.leyou.controller;

import com.alibaba.fastjson.JSONObject;
import com.fyf.leyou.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;
    /*method = RequestMethod.POST*/
    @RequestMapping(value = "/createOrder")
    /*, HttpServletRequest request*/
    public Map<String,Object> createOrder(@RequestBody String sku_id) {
       /* Map<String,Object> resultMap=new HashMap<>();
        Object userObj = request.getSession().getAttribute("user");
        if(userObj==null){
            resultMap.put("result",false);
            resultMap.put("msg","没有登陆不能购买！");
            return resultMap;
        }
        Map userMap = JSONObject.parseObject(userObj.toString(), Map.class);
        return orderService.createOrder(sku_id,userMap.get("user_id").toString());*/
        String sku_id1 = JSONObject.parseObject(sku_id).get("sku_id").toString();
        System.out.println(sku_id1);
        return orderService.createOrder(sku_id1,"28");
    }
    //根据order_id更新订单状态，根据sku_id减库存

    @RequestMapping(value = "/payOrder/{order_id}/{sku_id}",produces = "application/json")
    public Map<String, Object> payOrder(@PathVariable("order_id") String order_id, @PathVariable("sku_id") String sku_id){
        return orderService.payOrder(order_id,sku_id);
    }
    @RequestMapping(value = "/getOrder/{order_id}")
    public Map<String, Object> getOrder(@PathVariable("order_id") String order_id){
        return orderService.getOrder(order_id);
    }

}
