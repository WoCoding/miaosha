package com.fyf.leyou.controller;

import com.fyf.leyou.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Map<String, Object> login( String username,  String password, HttpServletRequest request){
        return userService.login(username,password,request);
    }

}
