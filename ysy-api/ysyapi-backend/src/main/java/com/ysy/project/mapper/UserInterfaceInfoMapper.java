package com.ysy.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ysy.ysyapicommon.model.entity.UserInterfaceInfo;

import java.util.List;

/**
* @author YSY
* @description 针对表【user_interface_info(用户接口调用关系表)】的数据库操作Mapper
* @createDate 2024-02-09 15:49:52
* @Entity generator.domain.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper
        extends BaseMapper<UserInterfaceInfo> {
    /**
     * 得到top limit个总调用次数最多的接口信息
     * @param limit
     * @return
     */
    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);

}




