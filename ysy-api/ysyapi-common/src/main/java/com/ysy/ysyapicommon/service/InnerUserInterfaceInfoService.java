package com.ysy.ysyapicommon.service;

/**
* @author YSY
* @description 针对表【user_interface_info(用户接口调用关系表)】的数据库操作Service
* @createDate 2024-02-09 15:49:52
*/
public interface InnerUserInterfaceInfoService {
    /**
     * 调用接口统计
     *
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);
}
