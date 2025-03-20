package com.chat.kit.exhandler;

import com.chat.kit.api.response.common.error.ErrorCode;
import com.chat.kit.customException.FcmException;
import com.chat.kit.customException.NoChatRoomException;
import com.chat.kit.customException.NoMemberException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExControllerAdvice {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NoChatRoomException.class)
    public ErrorResult BadChatRoomIdExHandler(NoChatRoomException e){
        log.error("[exceptionHandler] ex", e);
        return new ErrorResult(ErrorCode.BAD_REQUEST, e.getMessage());
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NoMemberException.class)
    public ErrorResult BadMemberIdExHandler(NoMemberException e){
        log.error("[exceptionHandler] ex", e);
        return new ErrorResult(ErrorCode.BAD_REQUEST, e.getMessage());
    }
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(FcmException.class)
    public ErrorResult FcmExHandler(FcmException e) {
        log.error("[exceptionHandler] FCM error", e);
        return new ErrorResult(ErrorCode.FCM_SEND_FAIL, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public ErrorResult exHandler(Exception e){
        log.error("[exceptionHandler] ex",e);
        return new ErrorResult(ErrorCode.INTERNAL_SERVER_ERROR,"내부오류");
    }
}
