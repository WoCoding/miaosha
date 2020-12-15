package com.fyf.leyou.service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface UserService {
    Map<String, Object> getUser(String username);
    Map<String, Object> addUser(String username, String password);
    Map<String, Object> login(String username, String password, HttpServletRequest request);
}
