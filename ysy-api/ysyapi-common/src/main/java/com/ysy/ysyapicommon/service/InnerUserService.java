package com.ysy.ysyapicommon.service;


import com.ysy.ysyapicommon.model.entity.User;


/**
 * 用户服务
 *
 * @author YSY
 */
public interface InnerUserService {
    /**
     * 从数据库中查询是否已分配给某个用户ak，并将查询到的user返回
     * 如果为null表示不存在
     * @param accessKey
     * @return
     */
    User getInvokeUser(String accessKey);
}
