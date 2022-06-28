package com.jiaxuan.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器
 */

@RestControllerAdvice
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {


    //异常处理方法
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException exception){
        log.info(exception.getMessage());

        if(exception.getMessage().contains("Duplicate entry")){
            String[] split = exception.getMessage().split(" ");
            String msg = "用户"+split[2] + "已存在！";
            return R.error(msg);
        }
        return R.error("未知错误！");
    }



    //异常处理方法
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException exception){
        log.info(exception.getMessage());

        return R.error(exception.getMessage());
    }


}
