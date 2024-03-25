package com.example.account.exception;

import com.example.account.dto.ErrorResponse;
import com.example.account.type.ErrorCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AccountException.class)
    public ErrorResponse handleAccountException(AccountException e){
        log.error("{} is occurred.",e.getErrorCodes());
        return new ErrorResponse(e.getErrorCodes(), e.getErrorMessage());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ErrorResponse HandlerDataIntegrityViolationException(DataIntegrityViolationException e){
        log.error("DataIntegrityViolationException is occurred",e);
        return new ErrorResponse(ErrorCode.INVALID_REQUEST,
                ErrorCode.INTERNAL_SERVER_ERROR.getDescription());
    }


    @ExceptionHandler(Exception.class)
    public ErrorResponse handleAccountException(Exception e){
        log.error("Exception is occurred.",e);
        return new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH.getDescription());
    }

}

