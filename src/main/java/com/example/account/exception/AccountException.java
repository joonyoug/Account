package com.example.account.exception;
import com.example.account.type.ErrorCode;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountException extends RuntimeException{
    private ErrorCode errorCodes;
    private  String errorMessage;

    public AccountException(ErrorCode errorCodes){
        this.errorCodes=errorCodes;
        this.errorMessage=errorCodes.getDescription();
    }


}
