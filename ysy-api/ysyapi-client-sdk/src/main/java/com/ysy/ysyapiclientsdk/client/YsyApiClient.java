package com.ysy.ysyapiclientsdk.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.ysy.ysyapiclientsdk.model.User;

import java.util.HashMap;
import java.util.Map;

import static com.ysy.ysyapiclientsdk.utils.SignUtils.getSign;

/**
 * 调用第三方接口的客户端
 *
 * @author YSY
 */
public class YsyApiClient {

    public static final String GATEWAY_HOST = "http://127.0.0.1:8090";

    private String accessKey;
    private String secretKey;

    public YsyApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public String getNameByGet(String name){
        //可以单独传入http参数，这样参数会自动做URL编码，拼接在URL中
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        String result= HttpUtil.get(GATEWAY_HOST + "/api/name/get", paramMap);
        System.out.println(result);
        return result;
    }

    public String getNameByPost(String name){
        //可以单独传入http参数，这样参数会自动做URL编码，拼接在URL中
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        String result= HttpUtil.post(GATEWAY_HOST + "/api/name/post", paramMap);
        System.out.println(result);
        return result;
    }

    public String getUsernameByPost(User user){
        String json = JSONUtil.toJsonStr(user);
        HttpResponse httpResponse = HttpRequest.post(GATEWAY_HOST + "/api/name/user")
                .addHeaders(getHeaderMap(json))
                .body(json)
                .execute();
        System.out.println(httpResponse.getStatus());
        String result = httpResponse.body();
        System.out.println(result);
        return result;
    }

    public Map<String, String> getHeaderMap(String body){
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("accessKey", accessKey);
        // secretKey一定不能直接发送
        //hashMap.put("secretKey", secretKey);
        hashMap.put("nonce", RandomUtil.randomNumbers(4));
        hashMap.put("body", body);
        hashMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        hashMap.put("sign", getSign(body, secretKey));
        return hashMap;
    }
}
