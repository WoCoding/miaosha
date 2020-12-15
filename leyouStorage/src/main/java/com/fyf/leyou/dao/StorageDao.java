package com.fyf.leyou.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface StorageDao {
    List<Integer> getStorageId(String sku_id);
    List<Map<String,Object>> getStockStorage(String sku_id);
    Integer addStorage(Map<String,Object> map);
    Integer addStorageHistory(Map<String,Object> map);
    Integer updateStorage(Map<String,Object> map);
}
