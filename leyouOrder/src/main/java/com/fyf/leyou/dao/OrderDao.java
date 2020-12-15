package com.fyf.leyou.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface OrderDao {
    Integer addOrder(Map<String, Object> orderMap);
    Integer addMessage(String sku_id);
    List<String> getMessage();
    Integer deleteMessage(String sku_id);
    Integer addOrderDetail(Map<String, Object> orderMap);
    Integer updateOrderStatus(String order_id);
    Map<String,Object> getOrder(String order_id);
}
