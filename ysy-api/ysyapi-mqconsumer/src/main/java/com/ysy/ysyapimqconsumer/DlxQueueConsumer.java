package com.ysy.ysyapimqconsumer;

import com.ysy.ysyapicommon.model.entity.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RabbitListener(queues = "dlx.queue")
public class DlxQueueConsumer {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String LOCK_KEY = "lock:invokeCount";


    @RabbitHandler
    public void process(MqMessage message) {
        log.info("接受到的消息："+message.toString());
        try {
            // 实现分布式锁
            RLock lock = redissonClient.getLock(LOCK_KEY);
            // 尝试获取锁，最多等待3秒，锁的租期为10秒
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                try {
                    // 执行业务逻辑
                    if(message.getRedisKey() == null || message.getHashKey() == null || message.getLogicOverTime() == null){
                        throw new RuntimeException("mq传递的参数为空");
                    }
                    Object value = stringRedisTemplate.opsForHash().get(message.getRedisKey(), message.getHashKey());
                    if(value == null) return;
                    Long currentTime = System.currentTimeMillis() / 1000;
                    if(Long.parseLong(value.toString()) + 30 * 60L <= currentTime){
                        stringRedisTemplate.opsForHash().delete(message.getRedisKey(), message.getHashKey());
                    }
                } finally {
                    lock.unlock(); // 释放锁
                }
            }
        } catch (Exception e){
            log.error("mq操作失败", e);
        }
    }
}
