package com.sky.interceptor;

import com.alibaba.fastjson.JSON;
import com.sky.annotation.CheckIdempotentAddOrder;
import com.sky.constant.CheckIdempotentConstant;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 检查幂等性校验的拦截器
 */
@Component
@Slf4j
public class CheckIdempotentAddOrderInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //如果请求的不是方法路径，那就直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        //强转handler
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        //获得请求目标的方法对象
        Method method = handlerMethod.getMethod();
        //获得注解
        CheckIdempotentAddOrder annotation = method.getAnnotation(CheckIdempotentAddOrder.class);
        log.info(String.valueOf(annotation));
        if(annotation != null){
            //进行删除token
            boolean b = false;
            try {
                return checkToken(request);
            } catch (Exception e) {
                writeResponseJson(response, e.getMessage());
            }
        }
        return true; //放行
    }

    /**
     * 校验Token
     * @param request
     * @return
     */
    private boolean checkToken(HttpServletRequest request) throws Exception {
        //获取请求头中的token，直接获取用户登录时的token
        String token = request.getHeader(jwtProperties.getUserTokenName());
        String key = CheckIdempotentConstant.ADD_ORDER_ORDER_REDIS + token + "_" + request.getServletPath();
        log.info(token);
        if(StringUtils.isEmpty(token)){
            //没有token
            throw new Exception("Illegal Request");
        }
        Boolean b = stringRedisTemplate.opsForValue().setIfAbsent(key, token, 3, TimeUnit.SECONDS);//添加订单的幂等性校验
//开始校验token并删除
        if(b){
            //提交订单成功
            return true;
        }else if (!b){
            log.info("提交订单失败");
            throw new Exception("提交订单失败");
        }
        return false;
    }

    /**
     * 拦截了并返回错误信息
     * @param response
     * @param msg
     */
    private void writeResponseJson(HttpServletResponse response, String msg) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(404);
        String jsonString = JSON.toJSONString(Result.error(msg));

        try (ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(jsonString.getBytes(StandardCharsets.UTF_8)); // 将 JSON 字符串转换为字节数组并写入
            outputStream.flush(); // 确保数据立即写入响应
        }

    }


}
