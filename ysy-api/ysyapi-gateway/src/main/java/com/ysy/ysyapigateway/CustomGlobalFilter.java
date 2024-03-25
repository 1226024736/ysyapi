package com.ysy.ysyapigateway;

import com.ysy.ysyapicommon.model.entity.InterfaceInfo;
import com.ysy.ysyapicommon.model.entity.MqMessage;
import com.ysy.ysyapicommon.model.entity.User;
import com.ysy.ysyapicommon.service.InnerInterfaceInfoService;
import com.ysy.ysyapicommon.service.InnerUserInterfaceInfoService;
import com.ysy.ysyapicommon.service.InnerUserService;
import com.ysy.ysyapigateway.exception.BusinessException;
import com.ysy.ysyapigateway.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ysy.ysyapiclientsdk.utils.SignUtils.getSign;

@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    private static final String LOCK_KEY = "lock:invokeCount";

    private static final String INTERFACE_HOST = "http://localhost:8123";

    private static final String REDIS_IP_BLACKLIST_KEY = "ip:blacklist";

    private static final String REDIS_NONCE_KEY = "nonce:time";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @DubboReference
    private InnerUserService innerUserService;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = INTERFACE_HOST + request.getPath().value();
        String method = request.getMethod().toString();
        // 1. 日志记录
        log.info("请求唯一标识：" + request.getId());
        log.info("请求路径：" + path);
        log.info("请求方法：" + method);
        log.info("请求参数：" + request.getQueryParams());
        String sourceAddress = request.getRemoteAddress().getHostString();
        log.info("请求来源地址：" + sourceAddress);
        // 2. 访问控制，出现在redis黑名单中的ip地址，阻止访问
        ServerHttpResponse response = exchange.getResponse();
        if(stringRedisTemplate.opsForSet().isMember(REDIS_IP_BLACKLIST_KEY, sourceAddress)){
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }
        // 3. 用户鉴权，ak，sk
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String body = headers.getFirst("body");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        // 根据实际情况去数据库查验accessKey
        User invokeUser = null;
        try{
            invokeUser = innerUserService.getInvokeUser(accessKey);
        }catch (Exception e){
            log.error("getInvokeUser error", e);
        }
        if (invokeUser == null) {
            return handleNoAuth(response);
        }
        // redis存储，随机数校验，如果在30分钟内有重复的，就拒绝访问
        Long currentTime = System.currentTimeMillis() / 1000;
        final Long NONCE_DURATION = 60 * 30L;// 30 分钟
        if(stringRedisTemplate.opsForHash().hasKey(REDIS_NONCE_KEY, nonce)){
            long frontTime = Long.parseLong(stringRedisTemplate.opsForHash().get(REDIS_NONCE_KEY, nonce).toString());
            // 如果redis中的随机数和取到的随机数一样且存储时间相差30分钟以内，返回错误信息
            if(currentTime - frontTime <= NONCE_DURATION)
                return handleNoAuth(response);
        }
        // 否则，更新redis
        stringRedisTemplate.opsForHash().put(REDIS_NONCE_KEY, nonce, currentTime.toString());
        // 发送消息至mq，使其30分钟后删除redis过期的消息
        Long logicOverTime = currentTime + NONCE_DURATION;
        MqMessage mqMessage = new MqMessage();
        mqMessage.setRedisKey(REDIS_NONCE_KEY);
        mqMessage.setHashKey(nonce);
        mqMessage.setLogicOverTime(logicOverTime);
        rabbitTemplate.convertAndSend("ttl.queue", mqMessage);
        // 时间和现在的时间不能超过 5 分钟
        final Long FIVE_MINUTES = 60 * 5L;
        if((currentTime - Long.parseLong(timestamp)) >= FIVE_MINUTES){
            return handleNoAuth(response);
        }
        // 从invokeUser获取secretKey，生成sign，进行校验
        String secretKey = invokeUser.getSecretKey();
        String serverSign = getSign(body, secretKey);
        if (sign == null || !sign.equals(serverSign)){
            return handleNoAuth(response);
        }
        // 4. 判断调用的接口是否存在
        // todo 先查询redis，再查数据库；若在redis直接返回，若不在，则先查询数据库再更新redis，返回
        // 从数据库中查询接口是否存在，以及请求方法是否匹配
        InterfaceInfo interfaceInfo = null;
        try{
            interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(path, method);
        }catch (Exception e){
            log.error("getInterfaceInfo error", e);
        }
        if (interfaceInfo == null) {
            return handleNoAuth(response);
        }

        // 5. 请求转发，调用接口 + 响应日志
        return handleResponse(exchange, chain, interfaceInfo.getId(), invokeUser.getId());
    }

    /**
     * 处理响应
     * @param exchange
     * @param chain
     * @return
     */
    public Mono<Void> handleResponse(ServerWebExchange exchange,
                                     GatewayFilterChain chain,
                                     long interfaceInfoId,
                                     long userId){
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓存数据的工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 拿到响应码
            HttpStatus statusCode = originalResponse.getStatusCode();
            if(statusCode == HttpStatus.OK){
                // 装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    // 等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里面写数据，super就是ServerHttpResponseDecorator父亲，即ServerHttpResponse
                            return super.writeWith(fluxBody.map(dataBuffer -> {
                                // 7. 调用成功，调用次数 + 1
                                boolean isOk = false;
                                try {
                                    // 实现分布式锁
                                    RLock lock = redissonClient.getLock(LOCK_KEY);
                                    // 尝试获取锁，最多等待3秒，锁的租期为10秒
                                    if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                                        try {
                                            // 执行业务逻辑
                                            isOk = innerUserInterfaceInfoService.invokeCount(interfaceInfoId, userId);
                                        } finally {
                                            lock.unlock(); // 释放锁
                                        }
                                    }
                                } catch (Exception e){
                                    log.error("invokeCount error", e);
                                }
                                if (!isOk){
                                    throw new BusinessException(ErrorCode.OPERATION_ERROR);
                                }
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);//释放掉内存
                                // 构建日志
                                StringBuilder sb2 = new StringBuilder(200);
                                List<Object> rspArgs = new ArrayList<>();
                                rspArgs.add(originalResponse.getStatusCode());
                                String data = new String(content, StandardCharsets.UTF_8);
                                sb2.append(data);
                                // 打印日志
                                log.info("响应结果：" + data);
                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            // 8. 调用失败
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 设置response为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange);//降级处理返回数据
        }catch (Exception e){
            log.error("网关处理响应异常：" + e);
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }

    public Mono<Void> handleNoAuth(ServerHttpResponse response){
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    public Mono<Void> handleInvokeError(ServerHttpResponse response){
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }
}