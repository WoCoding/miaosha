package com.fyf.leyou.service;

import java.util.Map;

public interface OrderService {
    Map<String, Object> createOrder(String sku_id, String user_id);
    Map<String,Object> addOrder(Map<String,Object> orderMap);
    Map<String, Object> payOrder(String order_id, String sku_id);
    Map<String,Object> getOrder(String order_id);
}

