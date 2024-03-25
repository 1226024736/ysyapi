package com.ysy.ysyapiinterface;


import com.ysy.ysyapiclientsdk.client.YsyApiClient;
import com.ysy.ysyapiclientsdk.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class YsyapiInterfaceApplicationTests {

    @Resource
    private YsyApiClient ysyApiClient;
    @Test
    void contextLoads() {
    }
    @Test
    void testYsyApiClient(){
        String result = ysyApiClient.getNameByGet("dingzhen");
        User user = new User();
        user.setUsername("dingzhen");
        String usernameByPost = ysyApiClient.getUsernameByPost(user);
        System.out.println(result);
        System.out.println(usernameByPost);

    }

}
