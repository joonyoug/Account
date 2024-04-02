package com.example.account.exception;
import com.example.account.type.ErrorCode;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor

@Builder
public class AccountException extends RuntimeException{
    private final ErrorCode errorCodes;
    private final  String errorMessage;

    public AccountException(ErrorCode errorCodes){
        this.errorCodes=errorCodes;
        this.errorMessage=errorCodes.getDescription();
    }


}
