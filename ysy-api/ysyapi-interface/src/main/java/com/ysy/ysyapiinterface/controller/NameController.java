package com.ysy.ysyapiinterface.controller;

import com.ysy.ysyapiclientsdk.model.User;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.ysy.ysyapiclientsdk.utils.SignUtils.getSign;


/**
 * 名称 API
 *
 * @author YSY
 */
@RestController
@RequestMapping("/name")
public class NameController {
    @GetMapping("/get")
    public String getNameByGet(String name, HttpServletRequest request){
        String author = request.getHeader("author");
        System.out.println(author);
        return "Get 你的名字是：" + name;
    }

    @PostMapping("/post")
    public String getNameByPost(@RequestParam(value = "name") String name){
        return "Post 你的名字是：" + name;
    }

    @PostMapping("/user")
    public String getUsernameByPost(@RequestBody User user, HttpServletRequest request){
//        String accessKey = request.getHeader("accessKey");
//        String nonce = request.getHeader("nonce");
//        String body = request.getHeader("body");
//        String timestamp = request.getHeader("timestamp");
//        String sign = request.getHeader("sign");
//        if (!accessKey.equals("dingzhen")){
//            throw new RuntimeException("无权限");
//        }
//        if (Long.parseLong(nonce) > 10000) {
//            throw new RuntimeException("无权限");
//        }
//        /*if (timestamp){
//
//        }*/
//        String serverSign = getSign(body, "xuebao");
//        if (!sign.equals(serverSign)){
//            throw new RuntimeException("无权限");
//        }
        String result = "Post 用户名字是：" + user.getUsername();
        // 调用成功，修改leftNum和totalNum

        return result;
    }

}
