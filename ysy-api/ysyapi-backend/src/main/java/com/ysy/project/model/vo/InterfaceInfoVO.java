package com.ysy.project.model.vo;

import com.ysy.ysyapicommon.model.entity.InterfaceInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口信息封装视图
 *
 * @author YSY
 * @TableName product
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceInfoVO extends InterfaceInfo {

    /**
     * 总共调用次数
     */
    private Integer totalNum;

    private static final long serialVersionUID = 1L;
}