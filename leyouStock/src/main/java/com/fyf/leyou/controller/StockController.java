package com.fyf.leyou.controller;


import com.alibaba.fastjson.JSONObject;
import com.fyf.leyou.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController

public class StockController {
    @Autowired
    StockService stockService;
    @RequestMapping(value = "/getStockList", produces = "application/json")
    public Map<String, Object> getStockList(){
        return stockService.getStockList();
    }
    @RequestMapping(value = "/getStock/{sku_id}", produces = "application/json")
    public Map<String, Object> getStock(@PathVariable String sku_id){
        return stockService.getStockById(sku_id);
    }
    @RequestMapping(value = "/insertLimitPolicy/{jsonObj}")
    public Map<String, Object> insertLimitPolicy(@PathVariable("jsonObj") String jsonObj){
        Map<String,Object> policyMap = JSONObject.parseObject(jsonObj,Map.class);
        return stockService.insertLimitPolicy(policyMap);
    }


}
