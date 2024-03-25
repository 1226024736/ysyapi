package com.ysy.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ysy.ysyapicommon.model.entity.InterfaceInfo;

/**
* @author YSY
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2024-01-28 17:12:48
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {
    /**
     * 校验接口信息
     * @param interfaceInfo
     * @param add
     */
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

}
