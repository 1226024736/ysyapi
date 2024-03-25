package com.ysy.ysyapicommon.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * mq消息
 */
@Data
public class MqMessage implements Serializable {

    private String RedisKey;

    private String HashKey;

    private Long LogicOverTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
