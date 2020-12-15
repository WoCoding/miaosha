package com.fyf.leyou.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Mapper
@Repository
public interface StockDao {
  List<Map<String, Object>> getStockList();
  List<Map<String, Object>> getStockById(String sku_id);
  boolean insertLimitPolicy(Map<String, Object> map);
}
