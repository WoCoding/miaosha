package com.fyf.leyou.service;

import com.fyf.leyou.dao.StorageDao;
import com.fyf.leyou.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StorageServiceImpl implements StorageService {
    @Autowired
    StorageDao storageDao;

    @Override
    @Transactional
    public Map<String, Object> insertStorage(String sku_id, double in_quanty, double out_quanty) {
        Map<String, Object> resultMap = new HashMap<>();
        //传入的参数
        if (sku_id.equals("")){
            resultMap.put("result", false);
            resultMap.put("msg", "商品的sku不能为空！");
            return resultMap;
        }

        if (in_quanty==0&&out_quanty==0){
            resultMap.put("result", false);
            resultMap.put("msg", "入库数量和出库数量不能同时为0！");
            return resultMap;
        }

        //根据商品id获取库存主表id
        List<Integer> list = storageDao.getStorageId(sku_id);
        //库存主表id
        Integer new_id = 0;
        double thisQuanty = in_quanty - out_quanty;
        Integer result=0;
        //如果库存主表id存在，获取id，写入库存历史表，并返回更新库存主表
        if (list != null && list.size() > 0) {
            new_id = list.get(0);
        }
        //如果库存主表id不存在，写入库存主表，获取id，写入库存历史表
        else {
            Map<String, Object> storageMap = new HashMap<>();
            storageMap.put("sku_id", sku_id);
            storageMap.put("thisQuanty", thisQuanty);
            result = storageDao.addStorage(storageMap);
            new_id = Integer.parseInt(storageMap.get("id").toString());
            //如果写入失败，返回错误信息 msg
            if (result == 0) {
                resultMap.put("result", false);
                resultMap.put("msg", "写入库存主表失败了！");
                return resultMap;
            }
        }
        //写入库存历史表（new_id,in_quanty, out_quanty）
        Map<String, Object> stHistoryMap = new HashMap<>();
        stHistoryMap.put("stock_storage_id", new_id);
        stHistoryMap.put("in_quanty", in_quanty);
        stHistoryMap.put("out_quanty", out_quanty);
        result=storageDao.addStorageHistory(stHistoryMap);
        //如果写入失败，返回错误信息 msg
        if (result == 0) {
            resultMap.put("result", false);
            resultMap.put("msg", "写入库存历史表失败了！");
            return resultMap;
        }
        //如果有库存，反回来更新主表库存
        if (list != null && list.size() > 0) {
            Map<String,Object> updateMap=new HashMap<>();
            updateMap.put("id",new_id);
            updateMap.put("thisQuanty",thisQuanty);
            result = storageDao.updateStorage(updateMap);
            //5.1、如果写入失败，返回错误信息 msg
            if (result==0){
                resultMap.put("result", false);
                resultMap.put("msg", "更新库存主表失败了！");
                return resultMap;
            }
        }
        //返回正常数据
        resultMap.put("result", true);
        resultMap.put("msg", "");
        return resultMap;
    }

    @Override
    public Map<String, Object> getStockStorage(String sku_id) {
        List<Map<String, Object>> list = storageDao.getStockStorage(sku_id);
        Map<String, Object> resultMap = new HashMap<>();
        if (list==null||list.isEmpty()){
            resultMap.put("result", false);
            resultMap.put("msg", "完了，服务器挂了，数据没取出来！");
            return resultMap;
        }
        resultMap.put("storage",list);
        resultMap.put("result", true);
        resultMap.put("msg", "");
        return resultMap;
    }
}
