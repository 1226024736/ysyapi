package com.ysy.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ysy.project.annotation.AuthCheck;
import com.ysy.project.common.BaseResponse;
import com.ysy.project.common.ErrorCode;
import com.ysy.project.common.ResultUtils;
import com.ysy.project.constant.UserConstant;
import com.ysy.project.exception.BusinessException;
import com.ysy.project.mapper.UserInterfaceInfoMapper;
import com.ysy.project.model.vo.InterfaceInfoVO;
import com.ysy.project.service.InterfaceInfoService;
import com.ysy.ysyapicommon.model.entity.InterfaceInfo;
import com.ysy.ysyapicommon.model.entity.UserInterfaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ysy.project.constant.UserConstant.*;

/**
 * 分析控制器
 *
 * @author YSY
 */
@RestController
@RequestMapping("/analysis")
@Slf4j
public class AnalysisController {

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @GetMapping("/top/interface/invoke")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<List<InterfaceInfoVO>> listTopInvokeInterfaceInfo() {
        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoMapper.listTopInvokeInterfaceInfo(3);
        Map<Long, List<UserInterfaceInfo>> interfaceInfoIdObjMap
                = userInterfaceInfoList
                .stream()
                .collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", interfaceInfoIdObjMap.keySet());
        List<InterfaceInfo> list = interfaceInfoService.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        List<InterfaceInfoVO> interfaceInfoVOList = list.stream().map(interfaceInfo -> {
            InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
            int totalNum = interfaceInfoIdObjMap.get(interfaceInfo.getId()).get(0).getTotalNum();
            interfaceInfoVO.setTotalNum(totalNum);
            return interfaceInfoVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(interfaceInfoVOList);

    }

}
