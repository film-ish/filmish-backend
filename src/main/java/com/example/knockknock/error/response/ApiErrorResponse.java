package com.example.knockknock.error.response;

import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.error.code.ErrorCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ApiErrorResponse extends ApiResponse {
    private ErrorCode code;
    public ApiErrorResponse(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public static ApiErrorResponse of(ErrorCode errorCode, String message){
        log.info("오류 메세지: " + message);
        return ApiErrorResponse.builder()
                .code(errorCode)
                .message(message)
                .build();
    }
}
