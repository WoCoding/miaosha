package com.fyf.leyou.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fyf.leyou.dao.StockDao;
import com.fyf.leyou.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class StockServiceImpl implements StockService {
    @Autowired
    StockDao stockDao;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RestTemplate restTemplate;

    @Override
    public Map<String, Object> getStockList() {
        Map<String, Object> resultMap = new HashMap<>();
        //查询商品列表
        List<Map<String, Object>> stockList = stockDao.getStockList();
        if (stockList == null || stockList.size() == 0) {
            resultMap.put("result", false);
            resultMap.put("msg", "查询结果为空！");
            return resultMap;
        }
        //对list的每个商品put进秒杀政策
        resultMap = getLimitPolicy(stockList);
        resultMap.put("sku_list", stockList);
        return resultMap;
    }

    @Override
    public Map<String, Object> getStockById(String sku_id) {
        Map<String, Object> resultMap = new HashMap<>();
        if (sku_id == null || sku_id.equals("")) {
            resultMap.put("result", false);
            resultMap.put("msg", "sku_id为空");
            return resultMap;
        }
        List<Map<String, Object>> list = stockDao.getStockById(sku_id);
        if (list == null || list.size() == 0) {
            resultMap.put("result", false);
            resultMap.put("msg", "查询结果为空");
            return resultMap;
        }
        //对list的每个商品put进秒杀政策
        resultMap = getLimitPolicy(list);
        resultMap.put("sku_list", list);
        return resultMap;
    }

    @Override
    @Transactional
    public Map<String, Object> insertLimitPolicy(Map<String, Object> policyMap) {
        Map<String, Object> resultMap = new HashMap<>();
        //前端传的map是空
        if (policyMap == null || policyMap.isEmpty()) {
            resultMap.put("result", false);
            resultMap.put("msg", "传入数据为空");
            return resultMap;
        }
        //在数据库中存入秒杀政策
        boolean result = stockDao.insertLimitPolicy(policyMap);
        //插入失败
        if (!result) {
            resultMap.put("result", false);
            resultMap.put("msg", "插入政策失败");
            return resultMap;
        }
        //获得当前时间和秒杀结束时间，获得有效时间
        long diff;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String now = restTemplate.getForObject("http://leyouTimeServer/getTime", String.class);
        String end = policyMap.get("end_time").toString();
        try {
            Date now_time = simpleDateFormat.parse(now);
            Date end_time = simpleDateFormat.parse(end);
            //结束时间-当前时间
            diff = (end_time.getTime() - now_time.getTime())/1000;
            if (diff < 0) {
                resultMap.put("result", false);
                resultMap.put("msg", "结束时间不能小于当前时间");
                return resultMap;
            }

        } catch (ParseException e) {
            resultMap.put("result", false);
            resultMap.put("msg", "日期转换失败");
            return resultMap;
        }
        //将秒杀政策存入reids
        // "LIMIT_POLICY_sku_id":"{"sku_id":123, "quanty":1000,"price":1000, "begin_time":"", "end_time":""}"

            //map转换为json字符串
        String policy=JSON.toJSONString(policyMap);
            //获得商品id
        String sku_id = policyMap.get("sku_id").toString();
        stringRedisTemplate.opsForValue().set("LIMIT_POLICY_"+sku_id,policy,diff,TimeUnit.SECONDS);
        //将商品信息存入reids
            //根据id查询商品
        List<Map<String, Object>> skus = stockDao.getStockById(sku_id);
        String sku = JSON.toJSONString(skus.get(0));
        stringRedisTemplate.opsForValue().set("SKU_"+sku_id,sku,diff,TimeUnit.SECONDS);
        //返回正常信息
        resultMap.put("result", true);
        resultMap.put("msg", "");
        return resultMap;

    }
    private Map<String, Object> getLimitPolicy(List<Map<String, Object>> stockList) {
        Map<String, Object> resultMap = new HashMap<>();
        //在redis中查询出每个商品的秒杀政策
        //遍历商品集合
        for (Map<String, Object> skuMap : stockList) {
            //从redis中取出该商品的政策
            String policy = stringRedisTemplate.opsForValue().get("LIMIT_POLICY_" + skuMap.get("sku_id"));
            //该商品在redis中有秒杀政策
            if(policy!=null&&!policy.equals("")) {
                //将政策的json字符串转换为map集合
                Map<String, Object> policyMap = JSONObject.parseObject(policy, Map.class);
                //给每个商品put进秒杀政策
                //表示秒杀已经开始，开始时间小于等于当前时间，并且当前时间小于等于结束时间
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String now = restTemplate.getForObject("http://leyou-time-server/getTime", String.class);
                try {
                    Date end_time = simpleDateFormat.parse(policyMap.get("end_time").toString());
                    Date begin_time = simpleDateFormat.parse(policyMap.get("begin_time").toString());
                    Date now_time = simpleDateFormat.parse(now);
                    //活动开始才获取数据
                    if (begin_time.getTime() <= now_time.getTime() && now_time.getTime() <= end_time.getTime()) {
                        skuMap.put("limitPrice", policyMap.get("price"));
                        skuMap.put("limitQuanty", policyMap.get("quanty"));
                        skuMap.put("limitBeginTime", policyMap.get("begin_time"));
                        skuMap.put("limitEndTime", policyMap.get("end_time"));
                        skuMap.put("nowTime", now);
                    }else {
                        //能取到redis，但是秒杀未开始
                        resultMap.put("result", false);
                        resultMap.put("msg", "秒杀未开始");
                        return resultMap;
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        resultMap.put("result", true);
        resultMap.put("msg", "");
        return resultMap;

    }

}
