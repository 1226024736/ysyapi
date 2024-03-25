package com.ysy.ysyapiclientsdk.utils;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;

/**
 * 签名工具
 */
public class SignUtils {
    /**
     * 生成签名
     * @param body
     * @param secretKey
     * @return
     */
    public static String getSign(String body, String secretKey){
        Digester sha = new Digester(DigestAlgorithm.SHA256);
        // 加密前内容
        String content = body + "." + secretKey;
        // 加密后
        String digestHex = sha.digestHex(content);
        return digestHex;
    }
}
