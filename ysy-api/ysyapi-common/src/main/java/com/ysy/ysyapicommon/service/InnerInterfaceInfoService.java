package com.ysy.ysyapicommon.service;

import com.ysy.ysyapicommon.model.entity.InterfaceInfo;

/**
* @author YSY
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2024-01-28 17:12:48
*/
public interface InnerInterfaceInfoService {
    /**
     * 根据请求路径和请求方法，查询数据库中是否存在该接口
     * 若存在返回接口信息，不存在返回null
     * @param path
     * @param method
     * @return
     */
    InterfaceInfo getInterfaceInfo(String path, String method);
}
