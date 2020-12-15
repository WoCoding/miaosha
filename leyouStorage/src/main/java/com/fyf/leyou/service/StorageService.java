package com.fyf.leyou.service;

import java.util.Map;

public interface StorageService {
    Map<String, Object> insertStorage(String sku_id, double in_quanty, double out_quanty);
    Map<String, Object> getStockStorage(String sku_id);
}
