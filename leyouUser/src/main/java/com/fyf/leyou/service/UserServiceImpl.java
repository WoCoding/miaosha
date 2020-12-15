package com.fyf.leyou.service;

import com.alibaba.fastjson.JSON;
import com.fyf.leyou.dao.UserDao;
import com.fyf.leyou.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Override
    public Map<String, Object> getUser(String username) {
        Map<String,Object> resultMap=new HashMap<>();
        //判断传入的参数是否有误
        if (username==null||username.equals("")){
            resultMap.put("result", false);
            resultMap.put("msg", "用户名不能为空！");
            return resultMap;
        }
        //获取会员列表
        List<Map<String, Object>> list = userDao.getUser(username);
        if (list==null||list.isEmpty()){
            resultMap.put("result", false);
            resultMap.put("msg", "没找到会员信息！");
            return resultMap;
        }
        resultMap=list.get(0);
        resultMap.put("result",true);
        resultMap.put("msg","");
        return resultMap;
    }

    @Override
    public Map<String, Object> addUser(String username, String password) {
        Map<String,Object> resultMap=new HashMap<>();
        if (username==null||username.equals("")){
            resultMap.put("result", false);
            resultMap.put("msg", "用户名不能为空！");
            return resultMap;
        }
        if (password==null||password.equals("")){
            resultMap.put("result", false);
            resultMap.put("msg", "密码不能为空！");
            return resultMap;
        }
        Map<String,Object> useMap=new HashMap<>();
        useMap.put("username",username);
        useMap.put("password",password);
        Integer result = userDao.addUser(useMap);
            if (result==0){
            resultMap.put("result", false);
            resultMap.put("msg", "数据库没有执行成功！");
            return resultMap;
        }
        Integer user_id = Integer.parseInt(useMap.get("id").toString());
        resultMap.put("user_id", user_id);
        resultMap.put("username", username);
        resultMap.put("phone", username);
        resultMap.put("password", password);
        resultMap.put("result", true);
        resultMap.put("msg", "");
        return resultMap;
    }

    @Override
    public Map<String, Object> login(String username, String password, HttpServletRequest request) {
        //取会员信息
        Map<String, Object> userMap = new HashMap<>();
        userMap= this.getUser(username);

        //没取到会员，新增会员信息
        if(!(Boolean)userMap.get("result")){
            userMap=this.addUser(username,password);
        }
        //存入session
        HttpSession session = request.getSession();
        String user = JSON.toJSONString(userMap);
       session.setAttribute("user", user);
        return userMap;
    }
}
