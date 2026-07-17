package org.example.back.config;

import org.example.back.common.BusinessException;
import org.example.back.common.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 捕获自定义业务异常
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMsg());
    }

    // 全局未知异常
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        return Result.error(500, "服务器内部异常");
    }
}