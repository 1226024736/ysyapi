package com.ysy.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ysy.ysyapicommon.model.entity.UserInterfaceInfo;

public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);
    public boolean invokeCount(long interfaceInfoId, long userId);
}
