package com.fyf.leyou.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface UserDao {
    List<Map<String, Object>> getUser(String username);
    Integer addUser(Map<String, Object> userMap);
}
