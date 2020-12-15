package com.fyf.leyou.service;

import java.util.List;
import java.util.Map;

public interface StockService {
    Map<String, Object> getStockList();
    Map<String, Object> getStockById(String sku_id);
    Map<String,Object> insertLimitPolicy(Map<String, Object> policyMap);
}
