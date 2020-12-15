package com.fyf.leyou.controller;

import com.fyf.leyou.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StorageController {
    @Autowired
    StorageService storageService;
    @RequestMapping(value = "/getStockStorage/{sku_id}",produces = "application/json")
    public Map<String, Object> getStockStorage(@PathVariable("sku_id") String sku_id){
        return storageService.getStockStorage(sku_id);
    }
    @RequestMapping(value = "/insertStorage/{sku_id}/{inquanty}/{outquanty}",produces = "application/json")
    public Map<String, Object> insertStorage(@PathVariable String sku_id,
                                             @PathVariable Double inquanty,
                                             @PathVariable Double outquanty){
        return storageService.insertStorage(sku_id,inquanty,outquanty);
    }
}
