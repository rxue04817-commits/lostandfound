package com.bean.lostandfound.handler;

import com.bean.lostandfound.exception.BaseException;
import com.bean.lostandfound.exception.NotFoundException;
import com.bean.lostandfound.exception.UnauthorizedException;
import com.bean.lostandfound.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler
    public Result exceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler
    public Result unauthorizedHandler(UnauthorizedException ex) {
        log.error("未授权：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler
    public Result notFoundHandler(NotFoundException ex) {
        log.error("资源不存在：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }
}
