package com.ysy.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.project.common.ErrorCode;
import com.ysy.project.exception.BusinessException;
import com.ysy.project.mapper.UserInterfaceInfoMapper;
import com.ysy.project.service.UserInterfaceInfoService;
import com.ysy.ysyapicommon.model.entity.UserInterfaceInfo;
import org.springframework.stereotype.Service;

/**
* @author YSY
* @description 针对表【user_interface_info(用户接口调用关系表)】的数据库操作Service实现
* @createDate 2024-02-09 15:49:52
*/
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
    implements UserInterfaceInfoService {

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建时，所有参数必须非空
        if (add) {
            if (userInterfaceInfo.getId() <= 0 ||
                    userInterfaceInfo.getUserId() <= 0 ||
                    userInterfaceInfo.getInterfaceInfoId() <= 0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "记录，接口或用户不存在");
            }
        }
        if (userInterfaceInfo.getLeftNum() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不能小于0");
        }
    }

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        // 判断
        if (interfaceInfoId <= 0 || userId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("interfaceInfoId", interfaceInfoId);
        updateWrapper.eq("userId", userId);
        updateWrapper.gt("leftNum", 0);
        updateWrapper.setSql("leftNum = leftNum - 1, totalNum = totalNum + 1");
        boolean update = this.update(updateWrapper);
        return update;
    }
}




