package com.ysy.project.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ysy.project.common.ErrorCode;
import com.ysy.project.exception.BusinessException;
import com.ysy.project.service.InterfaceInfoService;
import com.ysy.ysyapicommon.model.entity.InterfaceInfo;
import com.ysy.ysyapicommon.service.InnerInterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

@DubboService
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Override
    public InterfaceInfo getInterfaceInfo(String path, String method) {
        if (StringUtils.isAnyBlank(path, method)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url", path);
        queryWrapper.eq("method", method);
        InterfaceInfo interfaceInfo = interfaceInfoService.getOne(queryWrapper);
        return interfaceInfo;
    }
}
